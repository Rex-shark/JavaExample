// Devour Game - WebSocket-driven multiplayer eat game
(function(){
  const stage = document.getElementById('stage');
  const statusEl = document.getElementById('status');
  const roomTag = document.getElementById('roomTag');
  const wsUrlEl = document.getElementById('wsUrl');
  const chatBody = document.getElementById('chatBody');
  const scoreList = document.getElementById('scoreList');
  const aliveCountEl = document.getElementById('aliveCount');

  if (!stage) return;

  // config (grid-based)
  const GRID_SIZE = 25; // 25x25 board
  let cellSize = 0; // px per cell (computed)
  let boardOffsetX = 0, boardOffsetY = 0; // center board in stage

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

  // Chat helpers (avatar + system), keep existing behavior
  function appendChat(view){
    if (!chatBody || !view) return;
    const item = document.createElement('div');
    item.className = 'chat-msg' + (view.system ? ' chat-msg--system' : '');

    const avatar = document.createElement('div');
    avatar.className = 'avatar' + (!view.imageUrl ? ' avatar--fallback' : '');
    if (view.system){
      avatar.textContent = 'ℹ';
    } else if (view.imageUrl && String(view.imageUrl).trim()){
      const url = String(view.imageUrl).trim().replace(/"/g, '%22');
      avatar.style.backgroundImage = `url("${url}")`;
    } else {
      const initial = (view.nickname || '?').trim().charAt(0).toUpperCase();
      avatar.textContent = initial || '?';
    }

    const bubble = document.createElement('div');
    bubble.className = 'bubble';

    const header = document.createElement('div');
    header.className = 'bubble-header';
    if (view.system){
      header.innerHTML = `<strong>系統</strong>`;
    } else {
      header.innerHTML = `<strong>${escapeHtml(view.unitName||'')} - ${escapeHtml(view.nickname||'')}</strong>`;
    }

    const body = document.createElement('div');
    body.className = 'bubble-body';
    body.innerHTML = `${escapeHtml(view.text||'')}`;

    bubble.appendChild(header);
    bubble.appendChild(body);

    item.appendChild(avatar);
    item.appendChild(bubble);

    chatBody.appendChild(item);
    chatBody.scrollTop = chatBody.scrollHeight;
  }
  function appendSystem(text){ appendChat({ system:true, text }); }

  // players store: key = lineUserId (or composite), value = { gx, gy, el, unitName, nickname, imageUrl, alive, eat }
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

  // layout calculation
  function recalcLayout(){
    const rect = stage.getBoundingClientRect();
    const size = Math.min(rect.width, rect.height);
    // Use exact fractional cell size so 25x25 grid fills the stage (no leftover gap)
    cellSize = Math.max(8, size / GRID_SIZE); // removed floor to avoid shrinking board
    const boardSize = cellSize * GRID_SIZE; // should equal 'size'
    boardOffsetX = (rect.width - boardSize)/2;
    boardOffsetY = (rect.height - boardSize)/2;
    layoutCells();
    // reposition players
    for (const p of players.values()) positionPlayer(p);
  }

  function positionPlayer(p){
    if (!p || !p.el) return;
    const px = boardOffsetX + p.gx * cellSize;
    const py = boardOffsetY + p.gy * cellSize;
    const ps = Math.max(6, Math.floor(cellSize * 0.8));
    const inset = Math.floor((cellSize - ps)/2);
    p.el.style.width = `${ps}px`;
    p.el.style.height = `${ps}px`;
    p.el.style.left = `${px + inset}px`;
    p.el.style.top = `${py + inset}px`;
    p.el.style.borderRadius = `${Math.floor(ps/2)}px`;
  }

  window.addEventListener('resize', recalcLayout);

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
        appendSystem('遊戲開始！');
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
      appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: '遊戲已開始，無法加入' });
      return;
    }

    if (!p){
      p = ensurePlayer(u);
      // place at random non-overlapping grid position
      const pos = randomNonOverlapGrid();
      p.gx = pos.gx; p.gy = pos.gy; positionPlayer(p);
      appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: '加入了遊戲' });
      renderScore();
      updateAliveCount();
    } else if (!p.alive) {
      appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: '已被淘汰，無法重新加入' });
    }
    updateAliveCount();
  }

  // Poison ring implementation (grid)
  const poison = {
    cells: [], // flat list of {r,c,el,state}
    spiral: [],
    started: false,
    timer: null,
    warnMs: 100,
    stepMs: 200,
  };

  function createCells(){
    const layer = document.getElementById('cellsLayer');
    if (!layer) return;
    layer.innerHTML = '';
    poison.cells = [];
    for (let r=0; r<GRID_SIZE; r++){
      for (let c=0; c<GRID_SIZE; c++){
        const el = document.createElement('div');
        el.className = 'cell';
        layer.appendChild(el);
        poison.cells.push({ r, c, el, state:'normal' });
      }
    }
  }

  function layoutCells(){
    const layer = document.getElementById('cellsLayer');
    if (!layer || !poison.cells.length) return;
    for (const cell of poison.cells){
      const left = boardOffsetX + cell.c * cellSize;
      const top = boardOffsetY + cell.r * cellSize;
      cell.el.style.left = `${left}px`;
      cell.el.style.top = `${top}px`;
      cell.el.style.width = `${cellSize}px`;
      cell.el.style.height = `${cellSize}px`;
    }
  }

  function buildSpiral(){
    const rows = GRID_SIZE, cols = GRID_SIZE;
    const idx = [];
    let top=0, left=0, bottom=rows-1, right=cols-1;
    while (left <= right && top <= bottom){
      for (let c=left; c<=right; c++) idx.push({r:top,c});
      for (let r=top+1; r<=bottom; r++) idx.push({r,c:right});
      if (top < bottom){ for (let c=right-1; c>=left; c--) idx.push({r:bottom,c}); }
      if (left < right){ for (let r=bottom-1; r>top; r--) idx.push({r,c:left}); }
      top++; left++; bottom--; right--;
    }
    // map to index in poison.cells (row-major)
    poison.spiral = idx.map(({r,c}) => r*GRID_SIZE + c);
  }

  function startPoisonAfterDelay(){
    if (poison.started) return;
    poison.started = true;
    createCells();
    recalcLayout();
    buildSpiral();
    setTimeout(() => runPoisonLoop(), 10000);
  }

  function runPoisonLoop(){
    let i = 0;
    const loop = () => {
      if (playersLeft() <= 1){
        checkVictory(); // ensure victory overlay triggers when poison leaves one player
        return;
      }
      if (i >= poison.spiral.length){ return; }
      const cell = poison.cells[poison.spiral[i]];
      if (!cell){ return; }
      // warn
      setCellState(cell, 'warn');
      setTimeout(() => {
        // toxic
        setCellState(cell, 'toxic');
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
    el.classList.remove('cell--warn');
    el.classList.remove('cell--toxic');
    if (state === 'warn') el.classList.add('cell','cell--warn');
    if (state === 'toxic') el.classList.add('cell','cell--toxic');
  }

  function playersLeft(){
    let cnt = 0;
    for (const p of players.values()) if (p.alive) cnt++;
    return cnt;
  }

  function isToxicAtCoord(gx, gy){
    if (gx < 0 || gy < 0 || gx >= GRID_SIZE || gy >= GRID_SIZE) return false;
    const cell = poison.cells[gy*GRID_SIZE + gx];
    return !!cell && cell.state === 'toxic';
  }

  function eliminatePlayersOnCell(cell){
    let anyEliminated = false;
    for (const [_id, p] of players){
      if (!p.alive) continue;
      if (p.gx === cell.c && p.gy === cell.r){
        p.alive = false;
        if (p.el && p.el.parentNode) { p.el.parentNode.removeChild(p.el); }
        p.el = null;
        appendChat({ unitName: p.unitName, nickname: p.nickname, imageUrl: p.imageUrl, text: '被毒圈吞噬' });
        renderScore();
        updateAliveCount();
        anyEliminated = true;
      }
    }
    if (anyEliminated) checkVictory();
  }

  function ensurePlayer(user){
    const id = user && user.lineUserId ? String(user.lineUserId) : `${user.unitName}-${user.nickname}`;
    let p = players.get(id);
    if (!p){
      const el = document.createElement('div');
      el.className = 'player';
      const img = (user && typeof user.imageUrl === 'string' && user.imageUrl.trim()) ? user.imageUrl.trim().replace(/\"/g, '%22') : '';
      if (img) {
        el.style.backgroundImage = `url("${img}")`;
        el.style.borderColor = '#ffffff55';
      } else {
        const color = colorFromUnit(user.unitName || '');
        el.style.background = color.bg;
        el.style.borderColor = color.border;
      }
      stage.appendChild(el);
      p = { gx: 0, gy: 0, el, unitName: user.unitName||'', nickname: user.nickname||'', imageUrl: user.imageUrl||'', alive: true, eat: 0 };
      players.set(id, p);
    }
    return p;
  }

  function randomNonOverlapGrid(){
    // sample up to N times to avoid overlap
    for (let i=0;i<200;i++){
      const gx = Math.floor(Math.random() * GRID_SIZE);
      const gy = Math.floor(Math.random() * GRID_SIZE);
      let ok = true;
      for (const p of players.values()){
        if (!p.alive) continue;
        if (p.gx === gx && p.gy === gy){ ok=false; break; }
      }
      if (ok) return { gx, gy };
    }
    return { gx: 0, gy: 0 };
  }

  function playersAt(gx, gy){
    const list = [];
    for (const [id,p] of players){ if (p.alive && p.gx === gx && p.gy === gy) list.push([id,p]); }
    return list;
  }

  let gameOver = false;

  function checkVictory(){
    if (gameOver) return;
    if (!gameStarted) return;
    if (players.size < 2) return;
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
    const winnerBox = document.getElementById('winnerBox');
    if (winnerBox){
      winnerBox.innerHTML='';
      const wAvatar = document.createElement('div');
      wAvatar.className='winner-avatar';
      if (winner.imageUrl && winner.imageUrl.trim()){
        wAvatar.style.backgroundImage = `url("${winner.imageUrl.trim().replace(/\"/g,'%22')}")`;
        wAvatar.textContent='';
      } else {
        wAvatar.textContent = (winner.nickname||'?').trim().charAt(0).toUpperCase();
      }
      const wName = document.createElement('div'); wName.className='winner-name'; wName.textContent = `${winner.unitName} - ${winner.nickname}`;
      const wKills = document.createElement('div'); wKills.className='winner-kills'; wKills.textContent = `擊殺: ${winner.eat||0}`;
      winnerBox.appendChild(wAvatar);
      winnerBox.appendChild(wName);
      winnerBox.appendChild(wKills);
    }

    const wDiv = document.createElement('div');
    wDiv.innerHTML = `<strong>獲勝者:</strong> ${escapeHtml(winner.unitName)} ${escapeHtml(winner.nickname)} (擊殺:${winner.eat||0})`;
    if (listBox) listBox.appendChild(wDiv);

    const all = Array.from(players.values());
    const maxKill = Math.max(0, ...all.map(p => p.eat||0));
    if (maxKill >= 1){
      const tops = all.filter(p => (p.eat||0) === maxKill);
      const header = document.createElement('div');
      header.style.marginTop = '10px';
      header.innerHTML = `<strong>殺敵王 (擊殺數:${maxKill}):</strong>`;
      if (listBox) listBox.appendChild(header);
      const ul = document.createElement('ul');
      ul.style.paddingLeft = '20px';
      ul.style.margin = '6px 0 0 0';
      for (const p of tops){
        const li = document.createElement('li');
        li.textContent = `${p.unitName} ${p.nickname}`;
        ul.appendChild(li);
      }
      if (listBox) listBox.appendChild(ul);
    }

    overlay.style.display = 'flex';

    if (poison.timer) { clearTimeout(poison.timer); }

    const btnReplay = document.getElementById('btnReplay');
    if (btnReplay){ btnReplay.onclick = () => window.location.reload(); }
  }

  function handleMove(obj, dir){
    const u = obj.user || {};
    const id = u && u.lineUserId ? String(u.lineUserId) : `${u.unitName}-${u.nickname}`;
    let p = players.get(id);
    if (!p){ appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: '尚未加入遊戲' }); return; }
    if (!p.alive){ appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: '已被淘汰，無法行動' }); return; }
    if (!gameStarted){ appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: '遊戲尚未開始，無法行動' }); return; }

    let nx = p.gx, ny = p.gy;
    switch(dir){
      case 'up': ny = Math.max(0, p.gy - 1); break;
      case 'down': ny = Math.min(GRID_SIZE-1, p.gy + 1); break;
      case 'left': nx = Math.max(0, p.gx - 1); break;
      case 'right': nx = Math.min(GRID_SIZE-1, p.gx + 1); break;
      default: return;
    }

    // block entry if target is toxic
    if (isToxicAtCoord(nx, ny)){
      appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: '試圖進入毒圈被阻擋' });
      return;
    }

    // move
    p.gx = nx; p.gy = ny; positionPlayer(p);

    // collision: if landing on another player's cell, eat them
    for (const [otherId, other] of players){
      if (otherId === id) continue;
      if (!other.alive) continue;
      if (other.gx === p.gx && other.gy === p.gy){
        other.alive = false;
        if (other.el && other.el.parentNode) { other.el.parentNode.removeChild(other.el); }
        other.el = null;
        p.eat = (p.eat || 0) + 1;
        appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: `吞噬了 ${other.unitName || ''} - ${other.nickname || ''}` });
        renderScore();
        checkVictory();
        updateAliveCount();
      }
    }
  }

  function handleMessage(obj, text){
    const u = obj.user || {};
    const id = u && u.lineUserId ? String(u.lineUserId) : `${u.unitName}-${u.nickname}`;
    const p = players.get(id);
    if (!p){ appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: '尚未加入遊戲' }); return; }
    if (!p.alive){ appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: '已被淘汰，無法發言' }); return; }
    if (text && String(text).trim()){
      appendChat({ unitName: u.unitName, nickname: u.nickname, imageUrl: u.imageUrl, text: String(text) });
    }
  }

  function updateAliveCount(){
    if (!aliveCountEl) return;
    const total = players.size;
    let alive = 0; for (const p of players.values()) if (p.alive) alive++;
    aliveCountEl.textContent = `${alive}/${total}`;
  }

  // scoreboard renderer (unchanged presentation)
  function renderScore(){
    if (!scoreList) return;
    const arr = Array.from(players.values());
    arr.sort((a,b)=>{
      const ka = a.eat||0; const kb = b.eat||0;
      if (kb !== ka) return kb - ka; // higher kills first
      if (a.alive !== b.alive) return a.alive ? -1 : 1; // among same kills, alive first
      const unA = (a.unitName||'').localeCompare(b.unitName||'');
      if (unA !== 0) return unA;
      return (a.nickname||'').localeCompare(b.nickname||'');
    });
    scoreList.innerHTML = '';
    let rank = 1;
    const killRanked = Array.from(players.values()).slice().sort((a,b)=>(b.eat||0)-(a.eat||0));
    const rankMap = new Map();
    let currentRank = 0; let lastKills = null;
    for (const p of killRanked){
      const k = p.eat||0;
      if (lastKills === null || k !== lastKills){ currentRank++; lastKills = k; }
      rankMap.set(p, currentRank);
    }
    for (const p of arr){
      const li = document.createElement('li');
      li.className = 'score-item' + (p.alive ? '' : ' gray');
      const avatar = document.createElement('div');
      avatar.className = 'avatar';
      if (p.imageUrl && p.imageUrl.trim()){
        avatar.style.backgroundImage = `url("${p.imageUrl.trim().replace(/\"/g,'%22')}")`;
      } else {
        avatar.textContent = (p.nickname||'?').trim().charAt(0).toUpperCase();
      }
      let badge = null;
      const killRank = rankMap.get(p);
      if (killRank && killRank <= 3 && (p.eat||0) > 0){
        badge = document.createElement('div');
        badge.className = 'rank-badge rank-' + killRank;
        const span = document.createElement('span');
        span.textContent = killRank === 1 ? '🥇' : (killRank === 2 ? '🥈' : '🥉');
        badge.appendChild(span);
      }
      const metaWrap = document.createElement('div');
      metaWrap.style.display='flex';
      metaWrap.style.flex='1';
      metaWrap.style.alignItems='center';
      metaWrap.style.gap='6px';
      const rankSpan = document.createElement('span'); rankSpan.className='score-rank'; rankSpan.textContent = String(rank++);
      const nameSpan = document.createElement('span'); nameSpan.className='score-name'; nameSpan.textContent = `${p.unitName} - ${p.nickname}`;
      const countSpan = document.createElement('span'); countSpan.className='score-count'; countSpan.textContent = String(p.eat||0);
      if (badge){ metaWrap.appendChild(badge); }
      metaWrap.appendChild(rankSpan);
      metaWrap.appendChild(nameSpan);
      metaWrap.appendChild(countSpan);
      li.appendChild(avatar);
      li.appendChild(metaWrap);
      scoreList.appendChild(li);
    }
  }

  function colorFromUnit(name){
    const colors = [
      { bg:'#ff6b6b', border:'#ff4d4d' },
      { bg:'#6bff95', border:'#3de477' },
      { bg:'#9ecbff', border:'#6ba8ff' },
      { bg:'#f9a8d4', border:'#f472b6' },
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
          break;
      }
    });
  }

  connect();
  recalcLayout();
  updateAliveCount();

  // auto-start poison timing after start pressed
  (function(){
    const btn = document.getElementById('btnStart');
    if (!btn) return;
    btn.addEventListener('click', () => {
      const poll = setInterval(()=>{
        if (gameStarted){ clearInterval(poll); startPoisonAfterDelay(); }
      }, 200);
    });
  })();
})();
