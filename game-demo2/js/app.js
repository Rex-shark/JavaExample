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

  // initialize
  applyPos();
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
      let payload = ev.data;
      let obj = null;
      // Try parse JSON
      if (typeof payload === 'string') {
        try { obj = JSON.parse(payload); } catch(_) { /* not json */ }
      }
      // Determine command source precedence:
      // 1) JSON with dir field -> use that
      // 2) JSON with data field -> use that
      // 3) plain text payload -> use it
      let cmd = null;
      if (obj && typeof obj === 'object') {
        if (typeof obj.dir === 'string') {
          cmd = obj.dir;
        } else if (typeof obj.data === 'string') {
          cmd = obj.data;
        }
      }
      if (!cmd && typeof payload === 'string') {
        cmd = payload;
      }
      if (!cmd) return;

      // If full JSON with title/name/dir/text exists, append a chat line
      if (obj && typeof obj === 'object' && (obj.title || obj.name || obj.dir || obj.text)) {
        appendChat(obj);
      }

      handleCommand(String(cmd).toLowerCase());
    });
  }

  function handleCommand(cmd){
    const b = bounds();
    switch(cmd){
      case 'up': pos.y = clamp(pos.y - step, b.yMin, b.yMax); break;
      case 'down': pos.y = clamp(pos.y + step, b.yMin, b.yMax); break;
      case 'left': pos.x = clamp(pos.x - step, b.xMin, b.xMax); break;
      case 'right': pos.x = clamp(pos.x + step, b.xMin, b.xMax); break;
      default: return;
    }
    applyPos();
  }

  function appendChat(msgObj){
    if (!chatBody || !msgObj) return;
    const { title, name, dir, text, imageUrl } = msgObj;
    if (typeof title !== 'string' || typeof name !== 'string' || typeof dir !== 'string') return;

    const item = document.createElement('div');
    item.className = 'chat-msg';

    // avatar
    const avatar = document.createElement('div');
    avatar.className = 'avatar';
    if (typeof imageUrl === 'string' && imageUrl.trim() !== '') {
      // Use background-image for simple contain/cover fit
      const safeUrl = imageUrl.replace(/"/g, '%22');
      avatar.style.backgroundImage = `url("${safeUrl}")`;
    } else {
      avatar.classList.add('avatar--fallback');
      avatar.textContent = (name && name.trim()) ? name.trim().charAt(0).toUpperCase() : '?';
    }

    // message bubble/content
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    const header = document.createElement('div');
    header.className = 'bubble-header';
    header.innerHTML = `<strong>${escapeHtml(title)}-${escapeHtml(name)}</strong>`;
    const body = document.createElement('div');
    body.className = 'bubble-body';
    body.innerHTML = `${escapeHtml(dir)}${text ? ` <small>(${escapeHtml(text)})</small>` : ''}`;

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
      // Prefer new JSON-based endpoint move2 so server pushes {title,name,dir,text}
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
