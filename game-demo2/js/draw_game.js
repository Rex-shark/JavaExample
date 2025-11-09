// Draw Game front-end logic
(function(){
  // Elements
  const statusEl = document.getElementById('status');
  const roomTag = document.getElementById('roomTag');
  const wsUrlEl = document.getElementById('wsUrl');
  const chatBody = document.getElementById('chatBody');
  const btnStart = document.getElementById('btnStart');
  const btnExtend = document.getElementById('btnExtend');
  const inputTopicId = document.getElementById('inputTopicId');
  const levelStarsEl = document.getElementById('levelStars');
  const promptsBox = document.getElementById('promptsBox');
  const countdownEl = document.getElementById('countdown');
  const canvas = document.getElementById('drawCanvas');
  const btnClear = document.getElementById('btnClear');
  const btnDownload = document.getElementById('btnDownload');
  const colorPicker = document.getElementById('colorPicker');
  const sizePicker = document.getElementById('sizePicker');
  const btnEraser = document.getElementById('btnEraser');
  const btnPen = document.getElementById('btnPen');
  const winOverlay = document.getElementById('winOverlayDraw');
  const winList = document.getElementById('winList');
  const btnReplay = document.getElementById('btnReplay');
  const winnerBoxDraw = document.getElementById('winnerBoxDraw');
  const noWinnerDraw = document.getElementById('noWinnerDraw');
  const answerBoxDraw = document.getElementById('answerBoxDraw');

  if (!canvas) return;

  // Helpers
  function qs(key){ try { return new URL(window.location.href).searchParams.get(key); } catch(_) { return null; } }
  function escapeHtml(s){ if (s==null) return ''; return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#039;'); }
  function setStatus(text, cls){ if (!statusEl) return; statusEl.textContent = text; statusEl.classList.remove('ok','warn','err'); if (cls) statusEl.classList.add(cls); }

  // Room ID
  const roomId = qs('id') || 'default';
  if (roomTag) roomTag.textContent = roomId;

  // WS URL builder
  function buildWsUrl(){
    const scheme = location.protocol === 'https:' ? 'wss' : 'ws';
    const backend = qs('backend') || `${location.hostname}:8080`;
    return `${scheme}://${backend}/ws/game?id=${encodeURIComponent(roomId)}`;
  }

  // Game state
  let ws = null;
  let gameStarted = false;
  let prompts = [];
  let revealedCount = 0;
  let countdown = 0; // seconds
  let countdownTimer = null;
  let revealTimer = null;
  let baseCountdown = 80; // initial total seconds default
  const revealInterval = 20; // show next prompt every 20s

  // Guess tracking
  const guessTimes = new Map(); // userKey -> seconds to correct
  const wrongCounts = new Map(); // userKey -> wrong attempt count
  const userSnapshot = new Map(); // userKey -> {unitName,nickname,imageUrl}
  let startTimestamp = 0;

  // Canvas drawing setup
  const ctx = canvas.getContext('2d');
  let currentDpr = 1;
  function resizeCanvas(){
    const parent = canvas.parentElement; // .canvas-stage
    const rect = parent.getBoundingClientRect();
    const size = Math.floor(Math.min(rect.width, rect.height));
    const dpr = Math.max(1, window.devicePixelRatio || 1);
    currentDpr = dpr;
    ctx.setTransform(1,0,0,1,0,0);
    canvas.width = Math.max(1, Math.round(size * dpr));
    canvas.height = Math.max(1, Math.round(size * dpr));
    canvas.style.width = size + 'px';
    canvas.style.height = size + 'px';
    ctx.scale(dpr, dpr);
    redrawBg();
  }
  function redrawBg(){
    ctx.globalCompositeOperation = 'source-over';
    ctx.fillStyle = '#ffffff';
    const w = canvas.width / currentDpr;
    const h = canvas.height / currentDpr;
    ctx.clearRect(0,0,w,h);
    ctx.fillRect(0,0,w,h);
  }
  window.addEventListener('resize', resizeCanvas);
  resizeCanvas();

  let drawing = false; let lastX = 0, lastY = 0; let erasing = false;
  function getPos(evt){ const rect = canvas.getBoundingClientRect(); const t = evt.touches && evt.touches[0]; const x = (t?t.clientX:evt.clientX) - rect.left; const y = (t?t.clientY:evt.clientY) - rect.top; return {x,y}; }
  function strokeLine(x1,y1,x2,y2){ ctx.lineCap='round'; ctx.lineJoin='round'; ctx.strokeStyle = erasing ? '#ffffff' : (colorPicker? colorPicker.value : '#ffff00'); ctx.lineWidth = sizePicker ? (parseInt(sizePicker.value,10) || 6) : 6; ctx.globalCompositeOperation = 'source-over'; ctx.beginPath(); ctx.moveTo(x1,y1); ctx.lineTo(x2,y2); ctx.stroke(); }
  function pointerDown(e){ drawing = true; const p = getPos(e); lastX=p.x; lastY=p.y; strokeLine(lastX,lastY,lastX,lastY); e.preventDefault(); }
  function pointerMove(e){ if (!drawing) return; const p = getPos(e); strokeLine(lastX,lastY,p.x,p.y); lastX=p.x; lastY=p.y; e.preventDefault(); }
  function pointerUp(e){ drawing = false; e.preventDefault(); }
  canvas.addEventListener('mousedown', pointerDown); canvas.addEventListener('mousemove', pointerMove); window.addEventListener('mouseup', pointerUp);
  canvas.addEventListener('touchstart', pointerDown,{passive:false}); canvas.addEventListener('touchmove', pointerMove,{passive:false}); canvas.addEventListener('touchend', pointerUp);
  if (btnClear){ btnClear.addEventListener('click', () => redrawBg()); }
  if (btnDownload){ btnDownload.addEventListener('click', () => { try { const url = canvas.toDataURL('image/png'); const a = document.createElement('a'); a.href = url; a.download = `draw_game_${Date.now()}.png`; a.click(); } catch(e){ console.warn('Download failed', e); } }); }
  if (colorPicker){ colorPicker.value = '#ffff00'; }
  if (btnEraser){ btnEraser.addEventListener('click', () => { erasing=true; btnEraser.disabled=true; if(btnPen) btnPen.disabled=false; }); }
  if (btnPen){ btnPen.addEventListener('click', () => { erasing=false; btnPen.disabled=true; if(btnEraser) btnEraser.disabled=false; }); if (btnPen) btnPen.disabled=true; }

  // Topic API
  let drawAnswer = null; // store but never display
  async function loadTopic(){
    const id = (inputTopicId && inputTopicId.value) ? inputTopicId.value.trim() : '1';
    try {
      const url = `/game/draw_game?id=${encodeURIComponent(id)}`;
      const res = await fetch(url, { method:'GET' });
      const json = await res.json();
      if (!json || json.success !== true){ throw new Error((json && json.message) || 'API失敗'); }
      const data = json.data || {};
      drawAnswer = (typeof data.answer === 'string') ? data.answer.trim() : null;
      prompts = Array.isArray(data.prompts) ? data.prompts.slice() : [];
      const level = data.level || 0;
      renderLevel(level);
      renderPrompts();
      revealedCount = Math.min(prompts.length > 0 ? 1 : 0, prompts.length);
      updatePromptVisibility();
      setStatus('題目載入完成', 'ok');
    } catch(e){ setStatus('題目載入失敗', 'err'); console.warn(e); }
  }

  function renderLevel(level){ if (!levelStarsEl) return; const clamped = Math.max(0, Math.min(5, level|0)); levelStarsEl.textContent = clamped === 0 ? '-' : '⭐️'.repeat(clamped); }
  function renderPrompts(){ if (!promptsBox) return; promptsBox.innerHTML=''; for (let i=0;i<prompts.length;i++){ const tag=document.createElement('div'); tag.className='prompt-tag'; tag.dataset.idx=String(i); tag.textContent = prompts[i]; promptsBox.appendChild(tag);} }
  function updatePromptVisibility(){ if (!promptsBox) return; promptsBox.querySelectorAll('.prompt-tag').forEach(tag => { const i = parseInt(tag.dataset.idx,10); tag.style.display = (i < revealedCount) ? 'inline-flex' : 'none'; }); }

  function startRevealLoop(){ if (revealTimer) clearInterval(revealTimer); revealTimer = setInterval(() => { if (!gameStarted) return; if (revealedCount < prompts.length){ const elapsed = Math.floor((Date.now() - startTimestamp)/1000); const expected = 1 + Math.floor(elapsed / revealInterval); if (revealedCount < expected){ revealedCount = Math.min(expected, prompts.length); updatePromptVisibility(); } } }, 1000); }

  function startCountdown(){ countdown = baseCountdown; startTimestamp = Date.now(); updateCountdown(); if (countdownTimer) clearInterval(countdownTimer); countdownTimer = setInterval(() => { if (!gameStarted) return; countdown -= 1; if (countdown <= 0){ countdown = 0; updateCountdown(); clearInterval(countdownTimer); onGameEnd(); return; } updateCountdown(); }, 1000); }
  function updateCountdown(){ if (countdownEl) countdownEl.textContent = String(countdown); }

  if (btnExtend){ btnExtend.addEventListener('click', () => { if (!gameStarted) return; countdown += 15; updateCountdown(); appendSystem(`時間延長15秒 (剩餘 ${countdown} 秒)`); }); }

  function appendChat(view){
    if (!chatBody || !view) return;
    const item = document.createElement('div');
    item.className = 'chat-msg' + (view.system ? ' chat-msg--system' : '');

    // avatar wrapper
    const avatar = document.createElement('div');
    avatar.className = 'avatar';
    if (view.system) {
      avatar.textContent = 'ℹ';
    } else if (view.imageUrl && view.imageUrl.trim()) {
      avatar.style.backgroundImage = `url("${view.imageUrl.replace(/"/g,'%22')}")`;
    } else {
      avatar.textContent = (view.nickname||'?').trim().charAt(0).toUpperCase();
    }

    const bubble = document.createElement('div');
    bubble.style.flex = '1';
    const unitName = escapeHtml(view.unitName||'');
    const nickname = escapeHtml(view.nickname||'');
    const text = escapeHtml(view.text||'');

    if (view.system) {
      bubble.innerHTML = `<strong style="opacity:.9">系統</strong> ${text}`;
    } else {
      bubble.innerHTML = `<strong>${unitName} - ${nickname}</strong> ${text}`;
    }

    item.appendChild(avatar);
    item.appendChild(bubble);
    chatBody.appendChild(item);
    chatBody.scrollTop = chatBody.scrollHeight;
  }

  function appendSystem(text){ appendChat({ system:true, text }); }

  // WebSocket connect and handling
  function connect(){ const url = buildWsUrl(); if (wsUrlEl) wsUrlEl.textContent = url; setStatus('Connecting...', 'warn'); ws = new WebSocket(url); ws.addEventListener('open', () => setStatus('Connected', 'ok')); ws.addEventListener('close', () => setStatus('Disconnected', 'err')); ws.addEventListener('error', () => setStatus('Error', 'err')); ws.addEventListener('message', onWsMessage); }

  function onWsMessage(ev){ let obj=null; if (typeof ev.data==='string'){ try{ obj=JSON.parse(ev.data);}catch(_){} } if (!obj || typeof obj!=='object') return; const socketTypeRaw=obj.type; const socketType=(typeof socketTypeRaw==='string')? socketTypeRaw.toLowerCase() : 'user'; if (socketType==='unknown'){ console.log('[ignored UNKNOWN]', obj); return; } if (socketType==='system'){ console.log('[system reserved]', obj); return; } const u=obj.user||{}; const g=obj.game||{}; const gType=typeof g.type==='string' ? g.type.toLowerCase() : ''; const gText=typeof g.text==='string' ? g.text : undefined; switch(gType){ case 'message': if (gText && String(gText).trim()) appendChat({ unitName:u.unitName, nickname:u.nickname, imageUrl:u.imageUrl, text:gText }); break; case 'guess': handleGuess(u, gText); break; default: break; } }

  function userKey(user){ return user && user.lineUserId ? String(user.lineUserId) : `${user.unitName||''}-${user.nickname||''}`; }

  function rememberUser(user){ const key = userKey(user); if (!userSnapshot.has(key)){ userSnapshot.set(key, { unitName: user.unitName||'', nickname: user.nickname||'', imageUrl: user.imageUrl||'' }); } return key; }

  function handleGuess(user, text){ if (!text || !String(text).trim()) return; const trimmed = String(text).trim(); const key = rememberUser(user); let correct=false; if (drawAnswer){ correct = trimmed.toLowerCase() === String(drawAnswer).trim().toLowerCase(); } if (correct){ if (!guessTimes.has(key)){ const elapsed = Math.floor((Date.now() - startTimestamp)/1000); guessTimes.set(key, elapsed); } appendChat({ unitName:user.unitName, nickname:user.nickname, imageUrl:user.imageUrl, text:'答對了！💯' }); } else { wrongCounts.set(key, (wrongCounts.get(key)||0)+1); appendChat({ unitName:user.unitName, nickname:user.nickname, imageUrl:user.imageUrl, text:`猜 ${trimmed}！❌答錯啦~` }); } }

  function onGameEnd(){
    // Build ranking without printing to chat; show overlay with top 5 (ties allowed)
    const rows = [];
    const keys = new Set([...guessTimes.keys(), ...wrongCounts.keys()]);
    for (const k of keys){ const time = guessTimes.has(k) ? guessTimes.get(k) : null; const wrong = wrongCounts.get(k) || 0; const adjusted = time == null ? null : (time + wrong*5); rows.push({ key:k, time, wrong, adjusted }); }

    // Only consider answered players for winners
    const answered = rows.filter(r => r.adjusted != null);
    answered.sort((a,b)=> a.adjusted - b.adjusted);

    let display; // linter: avoid redundant initialization
    if (answered.length <= 5){ display = answered; }
    else {
      const cutScore = answered[4].adjusted; // 0-based index for 5th place
      display = answered.filter(r => r.adjusted <= cutScore);
    }

    // Answer box handling (always show if we have an answer)
    if (answerBoxDraw){
      const hasAns = !!(drawAnswer && String(drawAnswer).trim());
      if (hasAns){
        answerBoxDraw.style.display = 'flex';
        answerBoxDraw.innerHTML = `<span class="answer-label">正確答案</span> <span class="answer-text">${escapeHtml(String(drawAnswer))}</span>`;
      } else {
        answerBoxDraw.style.display = 'none';
        answerBoxDraw.textContent = '';
      }
    }

    // Winner box handling
    if (winnerBoxDraw && noWinnerDraw){
      winnerBoxDraw.innerHTML='';
      noWinnerDraw.style.display='none';
      if (answered.length === 0){
        winnerBoxDraw.style.display='none';
        noWinnerDraw.style.display='block';
      } else {
        const first = answered[0];
        const snapFirst = userSnapshot.get(first.key) || { unitName:'', nickname:'', imageUrl:'' };
        winnerBoxDraw.style.display='flex';
        const avatar = document.createElement('div');
        avatar.className='winner-avatar';
        if (snapFirst.imageUrl && snapFirst.imageUrl.trim()){
          avatar.style.backgroundImage = `url("${snapFirst.imageUrl.replace(/"/g,'%22')}")`;
          avatar.textContent='';
        } else {
          avatar.textContent = (snapFirst.nickname||'?').trim().charAt(0).toUpperCase();
        }
        const nm = document.createElement('div');
        nm.className='winner-name';
        nm.textContent = `${snapFirst.unitName} - ${snapFirst.nickname}`;
        const tm = document.createElement('div');
        tm.className='winner-time';
        tm.textContent = `用時 ${first.time}秒 + 罰時 ${first.wrong*5}秒 = ${first.adjusted}秒`;
        winnerBoxDraw.appendChild(avatar);
        winnerBoxDraw.appendChild(nm);
        winnerBoxDraw.appendChild(tm);
      }
    }

    if (winOverlay && winList){
      winList.innerHTML = '';
      for (const r of display){
        const snap = userSnapshot.get(r.key) || { unitName:'', nickname:'', imageUrl:'' };
        const item = document.createElement('div');
        item.className = 'win-item';
        const avatar = document.createElement('div');
        avatar.className = 'avatar';
        if (snap.imageUrl && snap.imageUrl.trim()){
          avatar.style.backgroundImage = `url("${snap.imageUrl.replace(/"/g, '%22')}")`;
        } else {
          avatar.textContent = (snap.nickname||'?').trim().charAt(0).toUpperCase();
        }
        const meta = document.createElement('div');
        meta.className = 'win-meta';
        const nm = document.createElement('div');
        nm.className = 'win-name';
        nm.textContent = `${snap.unitName} - ${snap.nickname}`;
        const tm = document.createElement('div');
        tm.className = 'win-time';
        tm.textContent = `用時 ${r.time}秒 + 罰時 ${r.wrong*5}秒 = ${r.adjusted}秒`;
        meta.appendChild(nm); meta.appendChild(tm);
        item.appendChild(avatar); item.appendChild(meta);
        winList.appendChild(item);
      }
      winOverlay.style.display = 'flex';
      if (btnReplay){ btnReplay.onclick = () => window.location.reload(); }
    }
  }

  function startGame(){ if (gameStarted) return; gameStarted = true; loadTopic().finally(() => { startTimestamp = Date.now(); startCountdown(); startRevealLoop(); if (btnStart) btnStart.disabled=true; appendSystem('遊戲開始'); }); }
  if (btnStart){ btnStart.addEventListener('click', startGame); }
  const btnCloseWin = document.getElementById('btnCloseWin');
  if (btnCloseWin && winOverlay){ btnCloseWin.addEventListener('click', () => { winOverlay.style.display='none'; }); }

  connect();
})();
