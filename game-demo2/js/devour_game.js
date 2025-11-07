// Devour Game - WebSocket-driven multiplayer eat game
(function(){
  const stage = document.getElementById('stage');
  const statusEl = document.getElementById('status');
  const roomTag = document.getElementById('roomTag');
  const wsUrlEl = document.getElementById('wsUrl');
  const chatBody = document.getElementById('chatBody');
  const scoreList = document.getElementById('scoreList');

  if (!stage) return;

  // config
  const step = 20; // px per move
  const radius = 11; // player radius (matches CSS 22px)

  // helpers
  function qs(key) {
    const url = new URL(window.location.href);
    return url.searchParams.get(key);
  }
  function escapeHtml(s){
    if (s == null) return '';
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }
  function clamp(n, min, max){ return Math.max(min, Math.min(max, n)); }
  function bounds(){
    const rect = stage.getBoundingClientRect();
    return {
      xMin: -Math.floor(rect.width/2) + radius,
      xMax:  Math.floor(rect.width/2) - radius,
      yMin: -Math.floor(rect.height/2) + radius,
      yMax:  Math.floor(rect.height/2) - radius,
    };
  }

  // players store: key = lineUserId (or composite), value = { x, y, el, unitName, nickname, alive, eat }
  const players = new Map();

  function setStatus(text, cls){
    statusEl.textContent = text;
    statusEl.classList.remove('ok','warn','err');
    if (cls) statusEl.classList.add(cls);
  }

  // room id
  const roomId = qs('id') || 'default';
  roomTag.textContent = roomId;

  // ws url
  function buildWsUrl() {
    const scheme = location.protocol === 'https:' ? 'wss' : 'ws';
    const backend = qs('backend') || `${location.hostname}:8080`;
    return `${scheme}://${backend}/ws/game?id=${encodeURIComponent(roomId)}`;
  }

  // create or get player element
  function ensurePlayer(user){
    const id = user && user.lineUserId ? String(user.lineUserId) : `${user.unitName}-${user.nickname}`;
    let p = players.get(id);
    if (!p){
      const el = document.createElement('div');
      el.className = 'player';
      const img = (user && typeof user.imageUrl === 'string' && user.imageUrl.trim()) ? user.imageUrl.trim().replace(/"/g, '%22') : '';
      if (img) {
        el.style.backgroundImage = `url("${img}")`;
        el.style.borderColor = '#ffffff55';
      } else {
        // fallback color based on unit
        const color = colorFromUnit(user.unitName || '');
        el.style.background = color.bg;
        el.style.borderColor = color.border;
      }
      el.style.top = '50%';
      el.style.left = '50%';
      stage.appendChild(el);
      p = { x: 0, y: 0, el, unitName: user.unitName||'', nickname: user.nickname||'', alive: true, eat: 0 };
      players.set(id, p);
    }
    return p;
  }

  function randomNonOverlapPos(){
    const b = bounds();
    // sample up to N times to avoid overlap
    for (let i=0;i<60;i++){
      const x = Math.floor(Math.random() * (b.xMax - b.xMin + 1)) + b.xMin;
      const y = Math.floor(Math.random() * (b.yMax - b.yMin + 1)) + b.yMin;
      let ok = true;
      for (const p of players.values()){
        if (!p.alive) continue;
        if (Math.abs(p.x - x) <= radius && Math.abs(p.y - y) <= radius){ ok = false; break; }
      }
      if (ok) return { x, y };
    }
    // fallback: center
    return { x: 0, y: 0 };
  }

  function applyPos(p){
    const b = bounds();
    p.x = clamp(p.x, b.xMin, b.xMax);
    p.y = clamp(p.y, b.yMin, b.yMax);
    p.el.style.transform = `translate(${p.x}px, ${p.y}px)`;
  }
  function safeApplyPos(p){ if (p && p.el) { applyPos(p); } }

  // keep positions in bounds when resizing
  window.addEventListener('resize', () => {
    for (const p of players.values()) safeApplyPos(p);
  });

  let gameStarted = false;

  function showCountdownAndStart(){
    const overlay = document.getElementById('countdownOverlay');
    const numEl = document.getElementById('countdownNum');
    const btn = document.getElementById('btnStart');
    if (!overlay || !numEl) { gameStarted = true; if (btn) btn.disabled = true; return; }
    if (btn) btn.disabled = true;
    overlay.style.display = 'flex';
    let n = 5;
    numEl.textContent = String(n);
    const timer = setInterval(() => {
      n -= 1;
      if (n <= 0){
        clearInterval(timer);
        overlay.style.display = 'none';
        gameStarted = true;
        appendDevourChat('遊戲開始！');
        return;
      }
      numEl.textContent = String(n);
    }, 1000);
  }

  // wire Start button
  (function(){
    const btn = document.getElementById('btnStart');
    if (!btn) return;
    btn.addEventListener('click', function(){
      if (gameStarted) return;
      showCountdownAndStart();
    });
  })();

  function handleJoin(obj){
    const u = obj.user || {};
    const id = u && u.lineUserId ? String(u.lineUserId) : `${u.unitName}-${u.nickname}`;
    let p = players.get(id);

    // Gate: after game started, new players cannot join
    if (gameStarted && !p){
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 遊戲已開始，無法加入`);
      return;
    }

    if (!p){
      p = ensurePlayer(u);
      // place at random non-overlapping position
      const pos = randomNonOverlapPos();
      p.x = pos.x; p.y = pos.y; safeApplyPos(p);
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 加入了遊戲`);
      renderScore();
    } else if (!p.alive) {
      // Eliminated players cannot rejoin
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 已被淘汰，無法重新加入`);
    }
  }

  // Poison ring implementation
  const poison = {
    cellSize: 20, // match grid background size
    cells: [], // {x,y,el,state:'normal'|'warn'|'toxic'} positioned by translate
    spiral: [], // indices order for toxic progression
    started: false,
    timer: null,
    warnMs: 100,
    stepMs: 200,
  };

  function buildCells(){
    const layer = document.getElementById('cellsLayer');
    if (!layer) return;
    layer.innerHTML = '';
    poison.cells = [];

    const rect = stage.getBoundingClientRect();
    const cols = Math.floor(rect.width / poison.cellSize);
    const rows = Math.floor(rect.height / poison.cellSize);

    // Origin centered: compute cell center positions in our translate coordinate (center is 0,0)
    const xMin = -Math.floor(cols/2) * poison.cellSize + (cols%2===0 ? poison.cellSize/2 : 0);
    const yMin = -Math.floor(rows/2) * poison.cellSize + (rows%2===0 ? poison.cellSize/2 : 0);

    for (let r=0; r<rows; r++){
      for (let c=0; c<cols; c++){
        const cx = xMin + c * poison.cellSize;
        const cy = yMin + r * poison.cellSize;
        const el = document.createElement('div');
        el.className = 'cell';
        el.style.transform = `translate(${cx}px, ${cy}px)`;
        layer.appendChild(el);
        poison.cells.push({ x: cx, y: cy, el, state: 'normal', r, c });
      }
    }

    // Build spiral indices from top-left going clockwise inward
    const idx = [];
    let top=0, left=0, bottom=rows-1, right=cols-1;
    while (left <= right && top <= bottom){
      for (let c=left; c<=right; c++) idx.push({r:top,c});
      for (let r=top+1; r<=bottom; r++) idx.push({r,c:right});
      if (top < bottom){ for (let c=right-1; c>=left; c--) idx.push({r:bottom,c}); }
      if (left < right){ for (let r=bottom-1; r>top; r--) idx.push({r,c:left}); }
      top++; left++; bottom--; right--;
    }
    // Map to cell indices
    poison.spiral = idx.map(({r,c}) => r*cols + c).filter(i => i >=0 && i < poison.cells.length);
  }

  function startPoisonAfterDelay(){
    if (poison.started) return;
    poison.started = true;
    // Build once at start
    buildCells();
    // Start 10s after game start
    setTimeout(() => runPoisonLoop(), 10000);
  }

  function runPoisonLoop(){
    let i = 0;
    const loop = () => {
      if (playersLeft() <= 1){ return; }
      if (i >= poison.spiral.length){ return; }
      const cell = poison.cells[poison.spiral[i]];
      if (!cell){ return; }
      // warn
      setCellState(cell, 'warn');
      setTimeout(() => {
        // toxic
        setCellState(cell, 'toxic');
        // eliminate players on this cell
        eliminatePlayersOnCell(cell);
        i++;
        poison.timer = setTimeout(loop, poison.stepMs);
      }, poison.warnMs);
    };
    loop();
  }

  function setCellState(cell, state){
    if (!cell || cell.state === state) return;
    cell.state = state;
    const el = cell.el;
    el.classList.remove('cell--warn','cell--toxic');
    if (state === 'warn') el.classList.add('cell','cell--warn');
    if (state === 'toxic') el.classList.add('cell','cell--toxic');
  }

  function playersLeft(){
    let cnt = 0;
    for (const p of players.values()) if (p.alive) cnt++;
    return cnt;
  }

  function isToxicAt(x, y){
    // Find the cell covering this coordinate; since cells are on a grid aligned by cellSize,
    // we can match by proximity within half cell size.
    for (const cell of poison.cells){
      if (cell.state !== 'toxic') continue;
      if (Math.abs(cell.x - x) < poison.cellSize/2 && Math.abs(cell.y - y) < poison.cellSize/2) return true;
    }
    return false;
  }

  function eliminatePlayersOnCell(cell){
    for (const [id, p] of players){
      if (!p.alive) continue;
      if (Math.abs(cell.x - p.x) < poison.cellSize/2 && Math.abs(cell.y - p.y) < poison.cellSize/2){
        // eliminate
        p.alive = false;
        if (p.el && p.el.parentNode) { p.el.parentNode.removeChild(p.el); }
        p.el = null;
        appendDevourChat(`${p.unitName || ''} - ${p.nickname || ''} 被毒圈吞噬`);
        renderScore();
      }
    }
  }

  // Extend movement blocking with toxic cells
  const _origApplyMove = handleMove;
  handleMove = function(obj, dir){
    const u = obj.user || {};
    const id = u && u.lineUserId ? String(u.lineUserId) : `${u.unitName}-${u.nickname}`;
    let p = players.get(id);
    if (!p || !p.alive) return _origApplyMove(obj, dir);
    if (!gameStarted){ return _origApplyMove(obj, dir); }

    const b = bounds();
    const next = { x: p.x, y: p.y };
    switch(dir){
      case 'up': next.y = clamp(p.y - step, b.yMin, b.yMax); break;
      case 'down': next.y = clamp(p.y + step, b.yMin, b.yMax); break;
      case 'left': next.x = clamp(p.x - step, b.xMin, b.xMax); break;
      case 'right': next.x = clamp(p.x + step, b.xMin, b.xMax); break;
      default: return _origApplyMove(obj, dir);
    }
    // block entry if next cell is toxic
    if (isToxicAt(next.x, next.y)){
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 試圖進入毒圈被阻擋`);
      return; // skip move
    }
    // perform original move flow (will also check collisions)
    return _origApplyMove(obj, dir);
  };

  // Trigger poison after game starts
  (function(){
    const btn = document.getElementById('btnStart');
    if (!btn) return;
    btn.addEventListener('click', () => {
      // start poison countdown when game starts; our showCountdownAndStart handles gameStarted flip.
      // We hook a small delay to poll gameStarted then schedule poison in 10s.
      const poll = setInterval(()=>{
        if (gameStarted){
          clearInterval(poll);
          startPoisonAfterDelay();
        }
      }, 200);
    });
  })();

  let gameOver = false;

  function checkVictory(){
    if (gameOver) return;
    const alive = Array.from(players.values()).filter(p => p.alive);
    if (alive.length === 1){
      gameOver = true;
      showVictoryOverlay(alive[0]);
    }
  }

  function showVictoryOverlay(winner){
    const overlay = document.getElementById('winOverlayDevour');
    if (!overlay) return;
    const listBox = overlay.querySelector('.win-list');
    if (listBox) listBox.innerHTML = '';

    // Winner info
    const wDiv = document.createElement('div');
    wDiv.innerHTML = `<strong>獲勝者:</strong> ${escapeHtml(winner.unitName)} ${escapeHtml(winner.nickname)} (擊殺:${winner.eat||0})`;
    if (listBox) listBox.appendChild(wDiv);

    // Top killers (>=1) - may include winner again, so exclude winner then re-add if multiple top
    const all = Array.from(players.values());
    const maxKill = Math.max(0, ...all.map(p => p.eat||0));
    if (maxKill >= 1){
      const tops = all.filter(p => (p.eat||0) === maxKill);
      // Remove winner from top list if present to avoid duplicate label; but spec says if multiple, show all
      // We'll show a separate block for 殺敵王; list all with maxKill.
      const header = document.createElement('div');
      header.style.marginTop = '6px';
      header.innerHTML = `<strong>殺敵王 (擊殺數:${maxKill}):</strong>`;
      if (listBox) listBox.appendChild(header);
      const ul = document.createElement('ul');
      ul.style.paddingLeft = '18px';
      ul.style.margin = '4px 0 0 0';
      for (const p of tops){
        const li = document.createElement('li');
        li.textContent = `${p.unitName} ${p.nickname}`;
        ul.appendChild(li);
      }
      if (listBox) listBox.appendChild(ul);
    }

    overlay.style.display = 'flex';

    // Stop poison loop
    if (poison.timer) { clearTimeout(poison.timer); }

    const btnReplay = document.getElementById('btnReplay');
    if (btnReplay){ btnReplay.onclick = () => window.location.reload(); }
    const btnShare = document.getElementById('btnShare');
    if (btnShare){
      btnShare.onclick = () => {
        try {
          navigator.clipboard.writeText(window.location.href);
          appendDevourChat('已複製分享連結');
        } catch(_) {
          appendDevourChat('無法複製分享連結');
        }
      };
    }
  }

  // Insert victory checks where players can die or be eaten
  const _origEliminatePlayersOnCell = eliminatePlayersOnCell;
  eliminatePlayersOnCell = function(cell){
    _origEliminatePlayersOnCell(cell);
    checkVictory();
  };

  const _origHandleMove = handleMove;
  handleMove = function(obj, dir){
    _origHandleMove(obj, dir);
    checkVictory();
  };

  function handleMove(obj, dir){
    const u = obj.user || {};
    const id = u && u.lineUserId ? String(u.lineUserId) : `${u.unitName}-${u.nickname}`;
    let p = players.get(id);
    if (!p){
      // not joined yet
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 尚未加入遊戲`);
      return;
    }
    if (!p.alive){
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 已被淘汰，無法行動`);
      return;
    }
    if (!gameStarted){
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 遊戲尚未開始，無法行動`);
      return;
    }

    const b = bounds();
    switch(dir){
      case 'up': p.y = clamp(p.y - step, b.yMin, b.yMax); break;
      case 'down': p.y = clamp(p.y + step, b.yMin, b.yMax); break;
      case 'left': p.x = clamp(p.x - step, b.xMin, b.xMax); break;
      case 'right': p.x = clamp(p.x + step, b.xMin, b.xMax); break;
      default: return;
    }
    applyPos(p);

    // collision: if landing on other player's cell, eat them
    for (const [otherId, other] of players){
      if (otherId === id) continue;
      if (!other.alive) continue;
      const dx = Math.abs(other.x - p.x);
      const dy = Math.abs(other.y - p.y);
      if (dx <= radius && dy <= radius){
        // eat
        other.alive = false;
        if (other.el && other.el.parentNode) { other.el.parentNode.removeChild(other.el); }
        other.el = null;
        p.eat = (p.eat || 0) + 1;
        appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 吞噬了 ${other.unitName || ''} - ${other.nickname || ''}`);
        renderScore();
      }
    }
  }

  function handleMessage(obj, text){
    const u = obj.user || {};
    const id = u && u.lineUserId ? String(u.lineUserId) : `${u.unitName}-${u.nickname}`;
    const p = players.get(id);
    if (!p){
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 尚未加入遊戲`);
      return;
    }
    if (!p.alive){
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''} 已被淘汰，無法發言`);
      return;
    }
    if (text && String(text).trim()){
      appendDevourChat(`${u.unitName || ''} - ${u.nickname || ''}: ${String(text)}`);
    }
  }

  // chat helper
  function appendDevourChat(text){
    if (!chatBody) return;
    const item = document.createElement('div');
    item.className = 'chat-msg';
    item.textContent = String(text == null ? '' : text);
    chatBody.appendChild(item);
    chatBody.scrollTop = chatBody.scrollHeight;
  }

  // scoreboard renderer
  function renderScore(){
    if (!scoreList) return;
    const arr = Array.from(players.values());
    arr.sort((a,b)=>{
      if (a.alive !== b.alive) return a.alive ? -1 : 1; // alive first
      return (b.eat||0) - (a.eat||0);
    });
    scoreList.innerHTML = '';
    let rank = 1;
    for (const p of arr){
      const li = document.createElement('li');
      li.className = 'score-item' + (p.alive ? '' : ' gray');
      li.innerHTML = `<span class="score-rank">${rank++}</span><span class="score-name">${escapeHtml(p.unitName)} - ${escapeHtml(p.nickname)}</span><span class="score-count">${p.eat||0}</span>`;
      scoreList.appendChild(li);
    }
  }

  function colorFromUnit(name){
    const colors = [
      { bg:'#ff6b6b', border:'#ff4d4d' }, // top-like
      { bg:'#6bff95', border:'#3de477' }, // right-like
      { bg:'#9ecbff', border:'#6ba8ff' }, // bottom-like
      { bg:'#f9a8d4', border:'#f472b6' }, // left-like
    ];
    let h = 0; for (let i=0;i<name.length;i++){ h = ((h*31) + name.charCodeAt(i)) | 0; }
    const idx = Math.abs(h) % colors.length;
    return colors[idx];
  }

  let ws;
  function connect(){
    const url = buildWsUrl();
    wsUrlEl.textContent = url;
    setStatus('Connecting...', 'warn');
    ws = new WebSocket(url);

    ws.addEventListener('open', () => setStatus('Connected', 'ok'));
    ws.addEventListener('close', () => setStatus('Disconnected', 'err'));
    ws.addEventListener('error', () => setStatus('Error', 'err'));

    ws.addEventListener('message', (ev) => {
      let obj = null;
      if (typeof ev.data === 'string'){
        try { obj = JSON.parse(ev.data); } catch(_) {}
      }
      if (!obj || typeof obj !== 'object') return;

      // Only process user-originated messages per SocketMessageType
      const typeRaw = obj.type;
      const socketType = (typeof typeRaw === 'string') ? typeRaw.toLowerCase() : 'user';
      if (socketType === 'unknown') { console.log('[ignored] UNKNOWN', obj); return; }
      if (socketType === 'system') { console.log('[system] reserved', obj); return; }

      const g = obj.game || {};
      const gType = typeof g.type === 'string' ? g.type.toLowerCase() : '';
      const gText = typeof g.text === 'string' ? g.text : undefined;

      switch (gType){
        case 'join':
          handleJoin(obj);
          break;
        case 'move':
          if (typeof gText === 'string') handleMove(obj, String(gText).toLowerCase());
          break;
        case 'message':
          handleMessage(obj, gText);
          break;
        default:
          // ignore others for now
          break;
      }
    });
  }

  connect();
})();
