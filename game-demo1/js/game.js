// Simple tile-based map + player movement with arrow keys
(() => {
  const canvas = document.getElementById('canvas');
  const ctx = canvas.getContext('2d');

  const TILE_SIZE = 32; // pixels
  const TILES = 16; // 16x16 => 512x512 canvas

  // Simple map: 0 grass, 1 water, 2 tree/rock (block)
  const map = Array.from({ length: TILES }, (_, y) =>
    Array.from({ length: TILES }, (_, x) => {
      // Create a simple pattern
      if ((x + y) % 7 === 0) return 1; // water
      if ((x * y) % 13 === 0) return 2; // obstacle
      return 0; // grass
    })
  );

  // Ensure spawn center is walkable
  const start = { x: Math.floor(TILES / 2), y: Math.floor(TILES / 2) };
  map[start.y][start.x] = 0;

  const player = {
    x: start.x,
    y: start.y,
    color: '#f59e0b',
  };

  function drawTile(x, y, type) {
    switch (type) {
      case 0: // grass
        ctx.fillStyle = '#166534';
        ctx.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        ctx.fillStyle = '#22c55e';
        ctx.globalAlpha = 0.15;
        ctx.fillRect(x * TILE_SIZE + 4, y * TILE_SIZE + 4, TILE_SIZE - 8, TILE_SIZE - 8);
        ctx.globalAlpha = 1;
        break;
      case 1: // water
        ctx.fillStyle = '#1d4ed8';
        ctx.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        ctx.fillStyle = '#93c5fd';
        ctx.globalAlpha = 0.15;
        ctx.fillRect(x * TILE_SIZE, y * TILE_SIZE + TILE_SIZE / 2, TILE_SIZE, TILE_SIZE / 2);
        ctx.globalAlpha = 1;
        break;
      case 2: // obstacle
        ctx.fillStyle = '#374151';
        ctx.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        ctx.fillStyle = '#9ca3af';
        ctx.globalAlpha = 0.2;
        ctx.fillRect(x * TILE_SIZE + 6, y * TILE_SIZE + 6, TILE_SIZE - 12, TILE_SIZE - 12);
        ctx.globalAlpha = 1;
        break;
    }

    // grid lines for RPG feel
    ctx.strokeStyle = 'rgba(0,0,0,.15)';
    ctx.strokeRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
  }

  function drawMap() {
    for (let y = 0; y < TILES; y++) {
      for (let x = 0; x < TILES; x++) {
        drawTile(x, y, map[y][x]);
      }
    }
  }

  function drawPlayer() {
    const px = player.x * TILE_SIZE + TILE_SIZE / 2;
    const py = player.y * TILE_SIZE + TILE_SIZE / 2;

    // simple character as a circle with a face
    ctx.fillStyle = player.color;
    ctx.beginPath();
    ctx.arc(px, py, TILE_SIZE * 0.35, 0, Math.PI * 2);
    ctx.fill();

    // eyes
    ctx.fillStyle = '#111827';
    ctx.beginPath();
    ctx.arc(px - 5, py - 4, 3, 0, Math.PI * 2);
    ctx.arc(px + 5, py - 4, 3, 0, Math.PI * 2);
    ctx.fill();
    // smile
    ctx.strokeStyle = '#111827';
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.arc(px, py + 2, 8, 0, Math.PI);
    ctx.stroke();
  }

  function canWalk(nx, ny) {
    // 僅做邊界檢查，避免因障礙導致遠端 API 觸發卻看起來沒移動
    return nx >= 0 && ny >= 0 && nx < TILES && ny < TILES;
  }

  function move(dx, dy) {
    const nx = player.x + dx;
    const ny = player.y + dy;
    if (canWalk(nx, ny)) {
      player.x = nx;
      player.y = ny;
      render();
    }
  }

  function render() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    drawMap();
    drawPlayer();
  }

  const keyMap = {
    ArrowUp: [0, -1],
    ArrowDown: [0, 1],
    ArrowLeft: [-1, 0],
    ArrowRight: [1, 0],
    w: [0, -1],
    s: [0, 1],
    a: [-1, 0],
    d: [1, 0]
  };

  window.addEventListener('keydown', (e) => {
    const key = e.key;
    if (keyMap[key]) {
      e.preventDefault();
      const [dx, dy] = keyMap[key];
      move(dx, dy);
    }
  });

  // --- WebSocket 連線到後端 WebSocketDemo ---
  function connectWS() {
    const params = new URLSearchParams(location.search);
    const roomId = params.get('id') || 'default';

    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    // 允許以全域變數或 query 指定 WS 主機；否則在本地開發時(3000)預設連 8080
    const qsBase = params.get('ws'); // 例如 ?ws=localhost:8080
    const hintedBase = (typeof window !== 'undefined' && window.GAME_WS_BASE) ? String(window.GAME_WS_BASE) : null;
    let host = qsBase || hintedBase || location.host;
    if (!qsBase && !hintedBase && location.hostname === 'localhost' && location.port === '3000') {
      host = 'localhost:8080';
    }

    const wsUrl = `${protocol}://${host}/ws/game?id=${encodeURIComponent(roomId)}`;
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      console.log('WS open', wsUrl);
    };

    ws.onmessage = (ev) => {
      try {
        const msg = JSON.parse(ev.data);
        if (msg.type === 'move' && typeof msg.dx === 'number' && typeof msg.dy === 'number') {
          console.debug('[WS] recv move', msg); // 調試用
          move(msg.dx, msg.dy);
        } else if (msg.type === 'message') {
          console.log('room message:', msg);
        } else if (msg.type === 'connected') {
          console.log('connected to room', msg.room);
        }
      } catch (e) {
        const t = String(ev.data || '').trim();
        if (t.startsWith('{') && t.includes('"type"') && t.includes('"move"')) {
          try {
            const obj = JSON.parse(t);
            console.debug('[WS] recv move(text)', obj); // 調試用
            if (obj && obj.type === 'move') move(+obj.dx || 0, +obj.dy || 0);
          } catch {}
        }
      }
    };

    ws.onclose = () => {
      console.warn('WS closed, retry in 1s');
      setTimeout(connectWS, 1000);
    };

    ws.onerror = () => {
      try { ws.close(); } catch {}
    };
  }

  render();
  connectWS();
})();
