// Replaced game logic with a simple CRUD frontend that talks to DockerDemo's /users API
(() => {
  const API_BASE = (window.GAME_API_BASE || 'http://localhost:8080') + '/users';
  document.getElementById('apiBase').textContent = API_BASE;

  const els = {
    form: document.getElementById('userForm'),
    account: document.getElementById('account'),
    password: document.getElementById('password'),
    remark: document.getElementById('remark'),
    editingUuid: document.getElementById('editingUuid'),
    submitBtn: document.getElementById('submitBtn'),
    cancelBtn: document.getElementById('cancelBtn'),
    notify: document.getElementById('notify'),
    usersTbody: document.getElementById('usersTbody')
  };

  function notify(msg, type = 'info') {
    const cls = type === 'error' ? 'alert-danger' : (type === 'success' ? 'alert-success' : 'alert-secondary');
    els.notify.innerHTML = `<div class="alert ${cls} p-2" role="alert">${escapeHtml(msg)}</div>`;
    setTimeout(() => { if (els.notify.innerHTML.includes(msg)) els.notify.innerHTML = ''; }, 4000);
  }

  function escapeHtml(s) { return String(s || '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'})[c]); }

  async function listUsers() {
    try {
      const res = await fetch(API_BASE);
      if (!res.ok) throw new Error('取得 users 失敗: ' + res.status);
      const data = await res.json();
      renderList(data || []);
    } catch (e) {
      notify('無法取得使用者: ' + e.message, 'error');
      console.error(e);
    }
  }

  function renderList(users) {
    els.usersTbody.innerHTML = '';
    if (!users.length) {
      els.usersTbody.innerHTML = '<tr><td colspan="4" class="text-center small-muted">無使用者資料</td></tr>';
      return;
    }
    users.forEach(u => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td style="max-width:260px;word-break:break-all">${escapeHtml(u.uuid)}</td>
        <td>${escapeHtml(u.account)}</td>
        <td>${escapeHtml(u.remark || '')}</td>
        <td>
          <button class="btn btn-sm btn-light me-1" data-act="edit" data-uuid="${escapeHtml(u.uuid)}">編輯</button>
          <button class="btn btn-sm btn-danger" data-act="del" data-uuid="${escapeHtml(u.uuid)}">刪除</button>
        </td>
      `;
      els.usersTbody.appendChild(tr);
    });
  }

  async function createUser(payload) {
    try {
      const res = await fetch(API_BASE, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) {
        const txt = await res.text();
        throw new Error('建立失敗: ' + res.status + ' ' + txt);
      }
      const created = await res.json();
      notify('建立成功', 'success');
      resetForm();
      listUsers();
    } catch (e) {
      notify(e.message, 'error');
    }
  }

  async function updateUser(uuid, payload) {
    try {
      const res = await fetch(API_BASE + '/' + encodeURIComponent(uuid), {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) {
        const txt = await res.text();
        throw new Error('更新失敗: ' + res.status + ' ' + txt);
      }
      const updated = await res.json();
      notify('更新成功', 'success');
      resetForm();
      listUsers();
    } catch (e) {
      notify(e.message, 'error');
    }
  }

  async function deleteUser(uuid) {
    if (!confirm('確定要刪除 ' + uuid + ' 嗎？')) return;
    try {
      const res = await fetch(API_BASE + '/' + encodeURIComponent(uuid), { method: 'DELETE' });
      if (!res.ok) {
        const txt = await res.text();
        throw new Error('刪除失敗: ' + res.status + ' ' + txt);
      }
      notify('刪除成功', 'success');
      listUsers();
    } catch (e) {
      notify(e.message, 'error');
    }
  }

  function resetForm() {
    els.editingUuid.value = '';
    els.account.value = '';
    els.password.value = '';
    els.remark.value = '';
    els.submitBtn.textContent = '建立';
    els.cancelBtn.style.display = 'none';
  }

  els.form.addEventListener('submit', (ev) => {
    ev.preventDefault();
    const uuid = els.editingUuid.value && els.editingUuid.value.trim();
    const account = els.account.value && els.account.value.trim();
    const password = els.password.value && els.password.value.trim();
    const remark = els.remark.value && els.remark.value.trim();

    if (!account || account.length < 6) { notify('account 長度不足'); return; }

    const payload = {
      uuid: uuid || undefined,
      account,
      password: password || 'changeme',
      remark: remark || undefined,
      createdUserId: 1
    };

    if (uuid) {
      updateUser(uuid, payload);
    } else {
      createUser(payload);
    }
  });

  els.cancelBtn.addEventListener('click', resetForm);

  els.usersTbody.addEventListener('click', (ev) => {
    const btn = ev.target.closest('button');
    if (!btn) return;
    const act = btn.getAttribute('data-act');
    const uuid = btn.getAttribute('data-uuid');
    if (act === 'edit') fetchUserToEdit(uuid);
    else if (act === 'del') deleteUser(uuid);
  });

  async function fetchUserToEdit(uuid) {
    try {
      const res = await fetch(API_BASE + '/' + encodeURIComponent(uuid));
      if (!res.ok) throw new Error('取得使用者失敗: ' + res.status);
      const u = await res.json();
      els.editingUuid.value = u.uuid;
      els.account.value = u.account || '';
      // 密碼不會回傳（JsonIgnore），前端留空表示不變或要改時再填
      els.password.value = '';
      els.remark.value = u.remark || '';
      els.submitBtn.textContent = '更新';
      els.cancelBtn.style.display = 'inline-block';
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (e) {
      notify(e.message, 'error');
    }
  }

  // initial
  listUsers();
})();
