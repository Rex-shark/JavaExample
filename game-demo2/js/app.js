// Basic WebSocket-driven move game
(function(){
  const stage = document.getElementById('stage');
  const player = document.getElementById('player');
  const statusEl = document.getElementById('status');
  const roomTag = document.getElementById('roomTag');
  const wsUrlEl = document.getElementById('wsUrl');
  const chatBody = document.getElementById('chatBody');

  if (!stage || !player) return;

  // config
  const step = 20; // px per move
  const halfPlayer = 12; // player half size (24/2)

  // helpers
  function qs(key) {
    const url = new URL(window.location.href);
    return url.searchParams.get(key);
  }

  // Escape HTML to prevent XSS when inserting dynamic text
  function escapeHtml(s){
    if (s == null) return '';
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Clamp helper
  function clamp(n, min, max){ return Math.max(min, Math.min(max, n)); }

  // Bounds relative to center origin
  function bounds(){
    const rect = stage.getBoundingClientRect();
    return {
      xMin: -Math.floor(rect.width/2) + halfPlayer,
      xMax:  Math.floor(rect.width/2) - halfPlayer,
      yMin: -Math.floor(rect.height/2) + halfPlayer,
      yMax:  Math.floor(rect.height/2) - halfPlayer,
    };
  }

  // position relative to center (0,0)
  let pos = { x: 0, y: 0 };

  function applyPos(){
    const b = bounds();
    pos.x = clamp(pos.x, b.xMin, b.xMax);
    pos.y = clamp(pos.y, b.yMin, b.yMax);
    player.style.transform = `translate(${pos.x}px, ${pos.y}px)`;
  }

  // --- targets & victory ---
  const targets = []; // { x, y, label, el }
  const halfTarget = 10; // target size 20x20
  let gameOver = false;
  let lastCommander = { unitName: '', nickname: '' }; // set on latest WS move

  function randomInt(min, max){ return Math.floor(Math.random() * (max - min + 1)) + min; }

  function createTarget(x, y, label, edge){
    const el = document.createElement('div');
    el.className = `target target--${edge}`;
    // Position relative to center like player
    el.style.transform = `translate(${x}px, ${y}px)`;
    stage.appendChild(el);
    targets.push({ x, y, label, el });
  }

  function initTargets(){
    // Clear existing (if any)
    targets.splice(0, targets.length);
    stage.querySelectorAll('.target').forEach(n => n.remove());

    const b = bounds();
    // Map edge -> label
    const labels = {
      top: '智慧軟體處',
      right: '產業研發處',
      bottom: '營業服務處+總經理室',
      left: '創新應用處+研發工程處',
    };

    // Top: y = yMin, x random in [xMin, xMax]
    createTarget(randomInt(b.xMin, b.xMax), b.yMin, labels.top, 'top');
    // Right: x = xMax, y random
    createTarget(b.xMax, randomInt(b.yMin, b.yMax), labels.right, 'right');
    // Bottom
    createTarget(randomInt(b.xMin, b.xMax), b.yMax, labels.bottom, 'bottom');
    // Left
    createTarget(b.xMin, randomInt(b.yMin, b.yMax), labels.left, 'left');
  }

  function aabbOverlap(ax, ay, ahw, ahh, bx, by, bhw, bhh){
    return Math.abs(ax - bx) <= (ahw + bhw) && Math.abs(ay - by) <= (ahh + bhh);
  }

  function checkWin(){
    if (!targets.length || gameOver) return null;
    for (const t of targets){
      if (aabbOverlap(pos.x, pos.y, halfPlayer, halfPlayer, t.x, t.y, halfTarget, halfTarget)){
        return t;
      }
    }
    return null;
  }

  function showWinDialog(dept, commander){
    // Build overlay or ensure it has content
    let overlay = document.getElementById('winOverlay');
    if (!overlay){
      overlay = document.createElement('div');
      overlay.id = 'winOverlay';
      overlay.className = 'win-overlay';
      document.body.appendChild(overlay);
    }
    // Ensure inner structure exists
    if (!overlay.querySelector('.win-box')){
      overlay.innerHTML = '<div class="win-box"><div class="win-title"></div><div class="win-sub"></div><div class="win-actions"><button id="btn-reload">重新開始</button></div></div>';
      const btn = overlay.querySelector('#btn-reload');
      if (btn){ btn.addEventListener('click', () => window.location.reload()); }
    }

    const title = overlay.querySelector('.win-title');
    const sub = overlay.querySelector('.win-sub');
    if (title) title.textContent = `恭喜${dept}獲勝！`;
    const u = commander || { unitName: '', nickname: '' };
    // 下一行顯示「單位名稱 - 姓名」
    if (sub) sub.textContent = `${u.unitName || '-'} - ${u.nickname || '-'}`;
    overlay.style.display = 'flex';
  }

  // initialize
  applyPos();
  initTargets();
  window.addEventListener('resize', applyPos);

  function setStatus(text, cls){
    statusEl.textContent = text;
    statusEl.classList.remove('ok','warn','err');
    if (cls) statusEl.classList.add(cls);
  }

  // room id from URL (?id=roomA)
  const roomId = qs('id') || 'default';
  roomTag.textContent = roomId;

  // optional debug identity
  const debugName = qs('name') || 'Rex';
  const debugTitle = qs('title') || '軟體處';

  // Build ws url
  function buildWsUrl() {
    const scheme = location.protocol === 'https:' ? 'wss' : 'ws';
    // backend assumed on same host:8080 for dev; allow override via ?backend
    const backend = qs('backend') || `${location.hostname}:8080`;
    return `${scheme}://${backend}/ws/game?id=${encodeURIComponent(roomId)}`;
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
      if (gameOver) return; // ignore inputs after victory

      let payload = ev.data;
      let obj = null;
      // Try parse JSON
      if (typeof payload === 'string') {
        try { obj = JSON.parse(payload); } catch(_) { /* not json */ }
      }

      let cmd = null;

      if (obj && typeof obj === 'object') {
        const looksLikeNew = ('user' in obj) || ('game' in obj) || ('success' in obj);
        if (looksLikeNew) {
          // Debug: print full SocketMessageResponse
          try { console.log('SocketMessageResponse:', obj); } catch(_) {}

          // New: branch by SocketMessageType (obj.type)
          // Backend sends enum as upper-case by default (e.g., "USER"/"SYSTEM"/"UNKNOWN");
          // we normalize to lower-case. Default to 'user' for backward compatibility when missing.
          const socketTypeRaw = obj.type;
          const socketType = (typeof socketTypeRaw === 'string') ? socketTypeRaw.toLowerCase() : 'user';

          if (socketType === 'unknown') {
            // UNKNOWN: ignore, only log for diagnostics
            try { console.log('[ignored] SocketMessageType=UNKNOWN', obj); } catch(_) {}
            return;
          }

          if (socketType === 'system') {
            // SYSTEM: reserved for future server-initiated system messages.
            // TODO: handle system notifications (e.g., room created, countdown, broadcast, etc.)
            // For now, keep a placeholder and optionally log.
            try { console.log('[system] message (reserved)', obj); } catch(_) {}
            return;
          }

          // USER: proceed with original game/message logic
          const u = obj.user || {};
          const g = obj.game || {};

          const type = typeof g.type === 'string' ? g.type.toLowerCase() : '';
          const gText = typeof g.text === 'string' ? g.text : undefined;

          // movement command when type === 'move'
          if (type === 'move' && typeof gText === 'string') {
            cmd = gText;
            // Remember commander for victory message
            lastCommander = { unitName: u.unitName || '', nickname: u.nickname || '' };
          }

          // Build a simplified view for chat rendering
          // - move: show dir = g.text, optional extra text from obj.message
          // - message: show only text = g.text
          const chatView = {
            unitName: u.unitName,
            nickname: u.nickname,
            dir: type === 'move' ? gText : undefined,
            text: (type === 'message')
              ? (gText && String(gText).trim() ? String(gText) : undefined)
              : ((obj.message == null || String(obj.message).trim() === '') ? undefined : String(obj.message)),
            imageUrl: u.imageUrl,
          };
          appendChat(chatView);
        } else {
          // Legacy format support: {title,name,dir,text} or {data}
          if (typeof obj.dir === 'string') {
            cmd = obj.dir;
          } else if (typeof obj.data === 'string') {
            cmd = obj.data;
          }
          if (obj.title || obj.name || obj.dir || obj.text) {
            appendChat(obj);
          }
        }
      }

      // Plain text fallback (e.g., 'up')
      if (!cmd && typeof payload === 'string') {
        cmd = payload;
      }
      if (!cmd) return;

      handleCommand(String(cmd).toLowerCase());
    });
  }

  function handleCommand(cmd){
    if (gameOver) return;
    const b = bounds();
    switch(cmd){
      case 'up': pos.y = clamp(pos.y - step, b.yMin, b.yMax); break;
      case 'down': pos.y = clamp(pos.y + step, b.yMin, b.yMax); break;
      case 'left': pos.x = clamp(pos.x - step, b.xMin, b.xMax); break;
      case 'right': pos.x = clamp(pos.x + step, b.xMin, b.xMax); break;
      default: return;
    }
    applyPos();

    const winTarget = checkWin();
    if (winTarget){
      gameOver = true;
      setStatus('Victory!', 'ok');
      showWinDialog(winTarget.label, lastCommander);
    }
  }

  function appendChat(msgObj){
    if (!chatBody || !msgObj) return;

    // Normalize fields for both new and legacy payloads
    const unitName = (typeof msgObj.unitName === 'string' && msgObj.unitName) || (typeof msgObj.title === 'string' && msgObj.title) || '';
    const nickname = (typeof msgObj.nickname === 'string' && msgObj.nickname) || (typeof msgObj.name === 'string' && msgObj.name) || '';
    const dir = (typeof msgObj.dir === 'string' && msgObj.dir) || (typeof msgObj.gameCommand === 'string' && msgObj.gameCommand) || '';
    const text = (typeof msgObj.text === 'string' && msgObj.text) ? msgObj.text : undefined;
    const imageUrl = (typeof msgObj.imageUrl === 'string' && msgObj.imageUrl) ? msgObj.imageUrl : undefined;

    // Require identity and at least one of dir or text
    if (!unitName || !nickname || (!dir && !text)) return;

    const item = document.createElement('div');
    item.className = 'chat-msg';

    // avatar
    const avatar = document.createElement('div');
    avatar.className = 'avatar';
    if (imageUrl) {
      const safeUrl = imageUrl.replace(/"/g, '%22');
      avatar.style.backgroundImage = `url("${safeUrl}")`;
    } else {
      avatar.classList.add('avatar--fallback');
      avatar.textContent = nickname.trim() ? nickname.trim().charAt(0).toUpperCase() : '?';
    }

    // message bubble/content
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    const header = document.createElement('div');
    header.className = 'bubble-header';
    header.innerHTML = `<strong>${escapeHtml(unitName)} - ${escapeHtml(nickname)}</strong>`;
    const body = document.createElement('div');
    body.className = 'bubble-body';

    if (dir && text) {
      body.innerHTML = `${escapeHtml(dir)} ${escapeHtml(text)}`;
    } else if (dir) {
      body.innerHTML = `${escapeHtml(dir)}`;
    } else if (text) {
      body.innerHTML = `${escapeHtml(text)}`;
    }

    bubble.appendChild(header);
    bubble.appendChild(body);

    item.appendChild(avatar);
    item.appendChild(bubble);

    chatBody.appendChild(item);
    chatBody.scrollTop = chatBody.scrollHeight;
  }

  // Debug panel: call /move API via dev proxy
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-cmd]');
    if (!btn) return;
    const cmd = btn.getAttribute('data-cmd');
    sendMove(cmd);
  });

  async function sendMove(cmd){
    try {
      // Prefer new JSON-based endpoint move2 so server pushes SocketMessageResponse
      const url = `/move/move2?id=${encodeURIComponent(roomId)}&name=${encodeURIComponent(debugName)}&title=${encodeURIComponent(debugTitle)}&dir=${encodeURIComponent(cmd)}`;
      const res = await fetch(url, { method: 'GET' });
      await res.text();
    } catch (e) {
      console.warn('sendMove failed', e);
    }
  }

  // Local keyboard control (not sending to server)
  window.addEventListener('keydown', (e) => {
    if (['ArrowUp','ArrowDown','ArrowLeft','ArrowRight'].includes(e.key)) {
      e.preventDefault();
      const map = { ArrowUp:'up', ArrowDown:'down', ArrowLeft:'left', ArrowRight:'right' };
      handleCommand(map[e.key]);
    }
  }, { passive:false });

  connect();
})();
