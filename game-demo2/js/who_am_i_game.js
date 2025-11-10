// who_am_i_game.js - Guess who I am game front-end
(function(){
  const statusEl = document.getElementById('status');
  const roomTag = document.getElementById('roomTag');
  const wsUrlEl = document.getElementById('wsUrl');
  const chatBody = document.getElementById('chatBody');
  const countdownEl = document.getElementById('countdown');
  const levelStarsEl = document.getElementById('levelStars');
  const promptCurrentEl = document.getElementById('promptCurrent');
  const questionImgEl = document.getElementById('questionImg');
  const stageHintEl = document.getElementById('stageHint');
  const btnStart = document.getElementById('btnStart');
  const btnExtend = document.getElementById('btnExtend');
  const inputTopicId = document.getElementById('inputTopicId');
  const winOverlay = document.getElementById('winOverlayWho');
  const answerPreview = document.getElementById('answerPreview');
  const answerImg = document.getElementById('answerImg');
  const answerName = document.getElementById('answerName');
  const rankList = document.getElementById('rankList');
  const btnCloseWin = document.getElementById('btnCloseWin');
  const btnReplay = document.getElementById('btnReplay');

  if (!btnStart || !roomTag) return; // page not loaded properly

  // State
  let ws;
  let gameStarted = false;
  let gameEnded = false;
  let topic = null; // WhoAmIGameModel
  let countdown = 0; // seconds left
  let countdownTimer = null;
  let promptSwitched = false; // switch to prompt image after 20s
  const playerStats = new Map(); // key=lineUserId or composite, value={wrong:0, answerMs:null, unitName, nickname, imageUrl}

  // State additions for progressive prompt reveal
  let promptsList = [];
  let revealedPromptCount = 0; // how many prompts currently revealed

  // helpers
  function qs(key) { try { const u = new URL(window.location.href); return u.searchParams.get(key); } catch(_) { return null; } }
  const roomId = qs('id') || 'default';
  roomTag.textContent = roomId;

  function buildWsUrl(){
    const scheme = location.protocol === 'https:' ? 'wss' : 'ws';
    const backend = qs('backend') || `${location.hostname}:8080`;
    return `${scheme}://${backend}/ws/game?id=${encodeURIComponent(roomId)}`;
  }

  function setStatus(text, cls){
    statusEl.textContent = text;
    statusEl.classList.remove('ok','warn','err');
    if (cls) statusEl.classList.add(cls);
  }

  function escapeHtml(s){
    if (s == null) return '';
    return String(s)
      .replace(/&/g,'&amp;')
      .replace(/</g,'&lt;')
      .replace(/>/g,'&gt;')
      .replace(/"/g,'&quot;')
      .replace(/'/g,'&#039;');
  }

  function appendChat(view){
    if (!chatBody || !view) return;
    const item = document.createElement('div');
    let extraClass = '';
    if (view.system) extraClass = ' chat-msg--system';
    if (view.correct) extraClass = ' chat-msg--correct';
    if (view.wrong) extraClass = ' chat-msg--wrong';
    item.className = 'chat-msg' + extraClass;

    const avatar = document.createElement('div');
    avatar.className = 'avatar' + (!view.imageUrl ? ' avatar--fallback' : '');
    if (view.system){
      avatar.textContent = 'ℹ';
    } else if (view.imageUrl && String(view.imageUrl).trim()){
      const url = String(view.imageUrl).trim().replace(/"/g, '%22');
      avatar.style.backgroundImage = `url("${url}")`;
    } else {
      avatar.textContent = (view.nickname||'?').trim().charAt(0).toUpperCase();
    }

    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    const header = document.createElement('div'); header.className = 'bubble-header';
    if (view.system){ header.innerHTML = `<strong>系統</strong>`; }
    else { header.innerHTML = `<strong>${escapeHtml(view.unitName||'')} - ${escapeHtml(view.nickname||'')}</strong>`; }
    const body = document.createElement('div'); body.className = 'bubble-body';
    body.innerHTML = escapeHtml(view.text||'');

    bubble.appendChild(header); bubble.appendChild(body);
    item.appendChild(avatar); item.appendChild(bubble);
    chatBody.appendChild(item);
    chatBody.scrollTop = chatBody.scrollHeight;
  }
  function appendSystem(text){ appendChat({ system:true, text }); }

  function formatStars(level){
    if (!level || level < 1) return '-';
    const cnt = Math.min(5, Math.max(1, level));
    return '⭐'.repeat(cnt);
  }

  function startCountdown(seconds){
    countdown = seconds;
    updateCountdown();
    if (countdownTimer) clearInterval(countdownTimer);
    countdownTimer = setInterval(()=>{
      if (gameEnded) { clearInterval(countdownTimer); return; }
      ensurePromptRevealByElapsed(); // progressive prompts reveal
      const elapsedSec = Math.floor((nowMs() - startMs)/1000);
      // Switch to prompt image once elapsed >=20s (updated requirement)
      if (!promptSwitched && elapsedSec >= 20 && topic && topic.promptImageUrl){
        promptSwitched = true;
        questionImgEl.src = topic.promptImageUrl;
        stageHintEl.textContent = '提示圖';
      }
      countdown -= 1;
      if (countdown <= 0){
        countdown = 0;
        updateCountdown();
        clearInterval(countdownTimer);
        endGame();
        return;
      }
      updateCountdown();
    }, 1000);
  }

  function updateCountdown(){
    if (countdownEl) countdownEl.textContent = String(countdown);
  }

  function extendTime(extra){
    if (gameEnded) return;
    countdown += extra;
    updateCountdown();
  }

  async function fetchTopic(topicId){
    try {
      // Use relative path so webpack devServer proxy (/game -> 8080) handles CORS avoidance.
      // We intentionally ignore ?backend for REST fetch to prevent cross-origin CORS block in dev.
      const url = `/game/who_am_i_game?id=${encodeURIComponent(topicId)}`;
      const res = await fetch(url, { method:'GET' });
      const data = await res.json();
      if (!data || data.success !== true){ appendSystem('取得題目失敗'); return null; }
      return data.data;
    } catch(e){ console.warn('fetchTopic error', e); appendSystem('題目載入錯誤'); return null; }
  }

  function renderTopic(model){
    topic = model;
    if (!model) return;
    levelStarsEl.textContent = formatStars(model.level);
    promptsList = Array.isArray(model.prompts) ? model.prompts.slice() : [];
    revealedPromptCount = promptsList.length > 0 ? 1 : 0; // show first immediately
    updatePromptDisplay();
    if (model.questionImageUrl){ questionImgEl.src = model.questionImageUrl; stageHintEl.textContent='題目圖'; }
    else { questionImgEl.removeAttribute('src'); stageHintEl.textContent=''; }
  }

  function updatePromptDisplay(){
    if (!promptCurrentEl) return;
    if (!promptsList || !promptsList.length){ promptCurrentEl.textContent = '--'; return; }
    // Join revealed prompts with '、'
    const slice = promptsList.slice(0, revealedPromptCount);
    promptCurrentEl.textContent = slice.join('、');
  }

  function ensurePromptRevealByElapsed(){
    if (!gameStarted || gameEnded) return;
    if (!promptsList || !promptsList.length) return;
    const elapsedSec = Math.floor((nowMs() - startMs)/1000); // seconds since game start
    // Reveal rule: at t>=0 show first (count=1); every full 15s add one more.
    const shouldCount = Math.min(promptsList.length, Math.floor(elapsedSec / 15) + 1);
    if (shouldCount > revealedPromptCount){
      revealedPromptCount = shouldCount;
      updatePromptDisplay();
    }
  }

  function ensurePlayer(user){
    const id = user && user.lineUserId ? String(user.lineUserId) : `${user.unitName}-${user.nickname}`;
    let p = playerStats.get(id);
    if (!p){
      p = { wrong:0, answerMs:null, unitName:user.unitName||'', nickname:user.nickname||'', imageUrl:user.imageUrl||'' };
      playerStats.set(id, p);
    }
    return p;
  }

  function nowMs(){ return Date.now(); }
  let startMs = 0;

  function handleGuess(obj, guessText){
    if (!gameStarted || gameEnded) return;
    if (!topic) return;
    const user = obj.user || {};
    const p = ensurePlayer(user);
    const displayGuess = String(guessText||'').trim();
    if (!displayGuess) return;
    const answers = Array.isArray(topic.answers) ? topic.answers : [];
    const isCorrect = answers.some(a => a && displayGuess && displayGuess.toLowerCase() === String(a).toLowerCase());
    if (isCorrect){
      if (p.answerMs == null){ p.answerMs = nowMs() - startMs; }
      appendChat({ unitName:p.unitName, nickname:p.nickname, imageUrl:p.imageUrl, correct:true, text:`${p.nickname}答對了！💯` });
    } else {
      p.wrong += 1;
      appendChat({ unitName:p.unitName, nickname:p.nickname, imageUrl:p.imageUrl, wrong:true, text:`${p.nickname}猜「${displayGuess}」❌答錯啦~` });
    }
  }

  function handleMessage(obj, text){
    const user = obj.user || {};
    const p = ensurePlayer(user);
    if (text && String(text).trim()){
      appendChat({ unitName:p.unitName, nickname:p.nickname, imageUrl:p.imageUrl, text:String(text) });
    }
  }

  function endGame(){
    if (gameEnded) return;
    gameEnded = true;
    // Switch stage image to answer image when time's up
    if (topic && topic.answerImageUrl) {
      try { questionImgEl.src = topic.answerImageUrl; } catch(_) {}
      if (stageHintEl) stageHintEl.textContent = '答案圖';
    }
    // Show answer name in stage area
    const stageAnswerName = document.getElementById('stageAnswerName');
    if (stageAnswerName) {
      stageAnswerName.textContent = topic && topic.displayName ? topic.displayName : '';
      stageAnswerName.style.display = topic && topic.displayName ? 'inline-flex' : 'none';
    }
    appendSystem('遊戲結束');
    showResultOverlay();
  }

  function showResultOverlay(){
    if (!winOverlay) return;
    // show answer image & name
    if (topic){
      if (topic.answerImageUrl){ answerImg.src = topic.answerImageUrl; }
      answerName.textContent = topic.displayName || '';
      answerPreview.style.display = 'flex';
    }
    // Build ranking: players with answerMs (time) sorted asc. Wrong penalty adds 5s each.
    const arr = Array.from(playerStats.values());
    const scored = arr.map(p => {
      let timeSec = p.answerMs != null ? Math.floor(p.answerMs / 1000) : null;
      let wrongPenalty = p.wrong * 5; // seconds
      let totalScore = (timeSec != null ? timeSec : (countdown + 9999)) + wrongPenalty; // fallback large number
      return { p, timeSec, wrongPenalty, totalScore };
    });
    scored.sort((a,b)=>{
      const aAnswered = a.p.answerMs != null; const bAnswered = b.p.answerMs != null;
      if (aAnswered && !bAnswered) return -1;
      if (!aAnswered && bAnswered) return 1;
      if (aAnswered && bAnswered){
        if (a.totalScore !== b.totalScore) return a.totalScore - b.totalScore;
      }
      // fallback by name
      const ua = a.p.unitName.localeCompare(b.p.unitName);
      if (ua !== 0) return ua;
      return a.p.nickname.localeCompare(b.p.nickname);
    });
    rankList.innerHTML='';
    for (const s of scored){
      const div = document.createElement('div'); div.className='rank-item';
      const avatar = document.createElement('div'); avatar.className='avatar';
      if (s.p.imageUrl && s.p.imageUrl.trim()){
        avatar.style.backgroundImage = `url("${s.p.imageUrl.trim().replace(/\"/g,'%22')}")`;
      } else { avatar.textContent = (s.p.nickname||'?').trim().charAt(0).toUpperCase(); }
      const meta = document.createElement('div'); meta.className='rank-meta';
      const name = document.createElement('div'); name.className='rank-name'; name.textContent = `${s.p.unitName} - ${s.p.nickname}`;
      const time = document.createElement('div'); time.className='rank-time'; time.textContent = (s.timeSec != null) ? `答題時間: ${s.timeSec}s` : '未答對';
      const wrong = document.createElement('div'); wrong.className='rank-score'; wrong.textContent = `答錯次數: ${s.p.wrong} (加時 ${s.wrongPenalty}s)`;
      const total = document.createElement('div'); total.className='rank-score'; total.textContent = (s.timeSec != null) ? `總成績: ${s.totalScore}s` : '-';
      meta.appendChild(name); meta.appendChild(time); meta.appendChild(wrong); meta.appendChild(total);
      div.appendChild(avatar); div.appendChild(meta);
      rankList.appendChild(div);
    }
    winOverlay.style.display='flex';
  }

  function resetGame(){
    // clear state for replay
    gameStarted = false; gameEnded = false; topic=null; promptSwitched=false; startMs=0;
    if (countdownTimer) clearInterval(countdownTimer); countdownTimer=null; countdown=0; updateCountdown();
    levelStarsEl.textContent='-'; promptCurrentEl.textContent='--'; questionImgEl.removeAttribute('src'); stageHintEl.textContent='';
    promptsList = []; revealedPromptCount = 0;
    playerStats.clear(); rankList.innerHTML=''; answerPreview.style.display='none';
    winOverlay.style.display='none';
    btnStart.disabled=false; appendSystem('已重置，請重新開始');
  }

  if (btnCloseWin){ btnCloseWin.onclick = () => { winOverlay.style.display='none'; }; }
  if (btnReplay){ btnReplay.onclick = () => { resetGame(); }; }

  async function startGame(){
    if (gameStarted) return; gameStarted=true; btnStart.disabled=true; appendSystem('遊戲開始');
    const id = (inputTopicId && inputTopicId.value.trim()) ? inputTopicId.value.trim() : '1';
    const model = await fetchTopic(id);
    if (!model){ appendSystem('題目取得失敗，遊戲終止'); gameEnded=true; return; }
    renderTopic(model);
    startMs = nowMs();
    startCountdown(30); // 30 seconds base
  }

  if (btnStart){ btnStart.addEventListener('click', () => startGame()); }
  if (btnExtend){ btnExtend.addEventListener('click', () => extendTime(15)); }

  // WebSocket connection (reuse logic shape from other games)
  function connect(){
    const url = buildWsUrl(); wsUrlEl.textContent = url; setStatus('Connecting...','warn');
    ws = new WebSocket(url);
    ws.addEventListener('open', () => setStatus('Connected','ok'));
    ws.addEventListener('close', () => setStatus('Disconnected','err'));
    ws.addEventListener('error', () => setStatus('Error','err'));
    ws.addEventListener('message', (ev) => {
      let obj=null; if (typeof ev.data === 'string'){ try { obj=JSON.parse(ev.data); } catch(_){} }
      if (!obj || typeof obj !== 'object') return;
      const socketTypeRaw = obj.type; const socketType = (typeof socketTypeRaw === 'string') ? socketTypeRaw.toLowerCase() : 'user';
      if (socketType === 'unknown') return; if (socketType === 'system') return;
      const g = obj.game || {}; const gType = typeof g.type === 'string' ? g.type.toLowerCase() : '';
      const gText = typeof g.text === 'string' ? g.text : undefined;
      switch (gType){
        case 'guess':
          handleGuess(obj, gText);
          // If at least one player answered correctly and all active players answered or time ended -> end
          break;
        case 'message':
          handleMessage(obj, gText);
          break;
        default:
          // ignore other types
          break;
      }
    });
  }

  connect();
})();
