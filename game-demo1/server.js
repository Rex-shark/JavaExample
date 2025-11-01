import express from 'express';
import compression from 'compression';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 3000;

app.use(compression());
app.use(express.json());

// Strict no-cache for API responses to avoid proxy/browser caching
function noCache(req, res, next) {
  res.set('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate');
  res.set('Pragma', 'no-cache');
  res.set('Expires', '0');
  res.set('Surrogate-Control', 'no-store');
  next();
}
app.use('/api', noCache);

// Simple in-memory move queue (FIFO)
const moveQueue = [];
const MAX_QUEUE = 50;

// SSE clients registry
const sseClients = new Set();
function broadcastMove(move) {
  const payload = `data: ${JSON.stringify({ type: 'move', move })}\n\n`;
  for (const res of sseClients) {
    try { res.write(payload); } catch { /* ignore broken pipe */ }
  }
}

function dirToDelta(dir) {
  const key = String(dir || '').toLowerCase();
  switch (key) {
    case 'arrowup':
    case 'up':
    case 'w':
      return { dx: 0, dy: -1 };
    case 'arrowdown':
    case 'down':
    case 's':
      return { dx: 0, dy: 1 };
    case 'arrowleft':
    case 'left':
    case 'a':
      return { dx: -1, dy: 0 };
    case 'arrowright':
    case 'right':
    case 'd':
      return { dx: 1, dy: 0 };
    default:
      return null;
  }
}

// API: enqueue a move from a single parameter `dir`
// Accepts: POST /api/move?dir=up  or JSON body { "dir": "left" }
app.post('/api/move', (req, res) => {
  const dir = req.query.dir ?? req.body?.dir;
  const delta = dirToDelta(dir);
  if (!delta) {
    return res.status(400).json({ error: 'Invalid dir. Use up|down|left|right or arrow keys or wasd.' });
  }
  moveQueue.push(delta);
  if (moveQueue.length > MAX_QUEUE) {
    // drop oldest to cap memory
    moveQueue.splice(0, moveQueue.length - MAX_QUEUE);
  }
  // push to SSE listeners immediately
  broadcastMove(delta);
  return res.json({ status: 'ok', enqueued: true, queueSize: moveQueue.length, move: delta });
});

// API: client polls for next move; returns and consumes one queued move or null
// GET /api/next-move -> { move: {dx,dy} | null }
app.get('/api/next-move', (req, res) => {
  const move = moveQueue.shift() || null;
  res.type('application/json');
  return res.json({ move });
});

// API: SSE stream for immediate move push
app.get('/api/stream', (req, res) => {
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.setHeader('X-Accel-Buffering', 'no'); // for nginx
  res.flushHeaders?.();

  // Send an initial comment to establish the stream
  res.write(': connected\n\n');

  sseClients.add(res);

  // Keep-alive ping every 25s
  const ping = setInterval(() => {
    try { res.write(': ping\n\n'); } catch {}
  }, 25000);

  req.on('close', () => {
    clearInterval(ping);
    sseClients.delete(res);
    try { res.end(); } catch {}
  });
});

// Static files
app.use(express.static(path.join(__dirname)));

// Fallback to index.html
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

app.listen(PORT, () => {
  console.log(`Server listening on http://localhost:${PORT}`);
});
