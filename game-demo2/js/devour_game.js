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

  function handleJoin(obj){
    const u = obj.user || {};
    const id = u && u.lineUserId ? String(u.lineUserId) : `${u.unitName}-${u.nickname}`;
    let p = players.get(id);
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
