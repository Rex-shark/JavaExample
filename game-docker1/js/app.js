(() => {
  const API_BASE = 'http://localhost:8080/users';

  // Elements
  const form = document.getElementById('user-form');
  const formTitle = document.getElementById('form-title');
  const uuidEl = document.getElementById('uuid');
  const accountEl = document.getElementById('account');
  const passwordEl = document.getElementById('password');
  const remarkEl = document.getElementById('remark');
  const createdUserIdEl = document.getElementById('createdUserId');
  const updateUserIdEl = document.getElementById('updateUserId');
  const submitBtn = document.getElementById('submit-btn');
  const resetBtn = document.getElementById('reset-btn');
  const cancelUpdateBtn = document.getElementById('cancel-update-btn');
  const statusEl = document.getElementById('status');
  const refreshBtn = document.getElementById('refresh-btn');
  const tableBody = document.querySelector('#users-table tbody');

  let editingUuid = null;

  function setStatus(msg, kind = 'info') {
    statusEl.textContent = msg || '';
    statusEl.className = `status ${kind}`;
  }

  function toText(v) { return v == null ? '' : String(v); }

  function renderUsers(users) {
    tableBody.innerHTML = '';
    if (!Array.isArray(users) || users.length === 0) {
      const tr = document.createElement('tr');
      const td = document.createElement('td');
      td.colSpan = 4;
      td.textContent = '無資料';
      td.className = 'muted center';
      tr.appendChild(td);
      tableBody.appendChild(tr);
      return;
    }

    users.forEach(u => {
      const tr = document.createElement('tr');

      const uuidTd = document.createElement('td');
      uuidTd.textContent = toText(u.uuid);
      tr.appendChild(uuidTd);

      const accountTd = document.createElement('td');
      accountTd.textContent = toText(u.account);
      tr.appendChild(accountTd);

      const remarkTd = document.createElement('td');
      remarkTd.textContent = toText(u.remark);
      tr.appendChild(remarkTd);

      const actionsTd = document.createElement('td');
      const editBtn = document.createElement('button');
      editBtn.type = 'button';
      editBtn.textContent = '編輯';
      editBtn.className = 'small';
      editBtn.addEventListener('click', () => startEdit(u));

      const delBtn = document.createElement('button');
      delBtn.type = 'button';
      delBtn.textContent = '刪除';
      delBtn.className = 'danger small';
      delBtn.addEventListener('click', () => deleteUser(u.uuid));

      actionsTd.appendChild(editBtn);
      actionsTd.appendChild(document.createTextNode(' '));
      actionsTd.appendChild(delBtn);

      tr.appendChild(actionsTd);
      tableBody.appendChild(tr);
    });
  }

  async function apiFetch(url, options = {}) {
    try {
      const res = await fetch(url, {
        headers: { 'Content-Type': 'application/json' },
        ...options,
      });
      const contentType = res.headers.get('content-type') || '';
      const isJson = contentType.includes('application/json');
      const payload = isJson ? await res.json() : null;

      if (!res.ok) {
        const msg = payload?.message || `HTTP ${res.status}`;
        throw new Error(msg);
      }
      return payload; // ApiResponse envelope
    } catch (err) {
      throw err;
    }
  }

  async function loadUsers() {
    setStatus('讀取使用者列表中...');
    try {
      const resp = await apiFetch(API_BASE);
      renderUsers(resp?.data || []);
      setStatus(`載入完成，共 ${Array.isArray(resp?.data) ? resp.data.length : 0} 筆。`, 'success');
    } catch (e) {
      setStatus(`讀取失敗：${e.message}`, 'error');
    }
  }

  function clearForm() {
    form.reset();
    uuidEl.value = '';
    editingUuid = null;
    submitBtn.textContent = '新增';
    formTitle.textContent = '建立使用者';
    cancelUpdateBtn.classList.add('hidden');
  }

  function startEdit(u) {
    editingUuid = u.uuid;
    uuidEl.value = toText(u.uuid);
    accountEl.value = toText(u.account);
    passwordEl.value = '';
    remarkEl.value = toText(u.remark);
    // Keep createdUserId as-is; set updateUserId for PUT
    submitBtn.textContent = '儲存更新';
    formTitle.textContent = '更新使用者';
    cancelUpdateBtn.classList.remove('hidden');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelUpdateBtn.addEventListener('click', clearForm);
  resetBtn.addEventListener('click', (e) => { e.preventDefault(); clearForm(); });
  refreshBtn.addEventListener('click', () => loadUsers());

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    // Basic validation via HTML attributes; additionally check here
    if (!accountEl.value || accountEl.value.length < 6) {
      setStatus('帳號至少 6 碼。', 'error');
      accountEl.focus();
      return;
    }
    if (!passwordEl.value && !editingUuid) {
      setStatus('請輸入密碼。', 'error');
      passwordEl.focus();
      return;
    }

    const payloadCreate = {
      uuid: uuidEl.value || undefined,
      account: accountEl.value,
      password: passwordEl.value || undefined,
      remark: remarkEl.value || undefined,
      createdUserId: Number(createdUserIdEl.value) || 1,
      updateUserId: Number(updateUserIdEl.value) || undefined,
    };

    try {
      if (!editingUuid) {
        setStatus('建立中...');
        const resp = await apiFetch(API_BASE, {
          method: 'POST',
          body: JSON.stringify(payloadCreate),
        });
        setStatus(`建立成功（uuid=${resp?.data?.uuid || 'N/A'}）`, 'success');
        clearForm();
        await loadUsers();
      } else {
        // For update, backend expects UserBase fields; password may be required by service; include if provided
        const payloadUpdate = {
          uuid: editingUuid,
          account: accountEl.value,
          password: passwordEl.value || undefined,
          remark: remarkEl.value || undefined,
          createdUserId: Number(createdUserIdEl.value) || 1,
          updateUserId: Number(updateUserIdEl.value) || 1,
        };
        setStatus('更新中...');
        const resp = await apiFetch(`${API_BASE}/${encodeURIComponent(editingUuid)}`, {
          method: 'PUT',
          body: JSON.stringify(payloadUpdate),
        });
        setStatus(`更新成功（uuid=${resp?.data?.uuid || editingUuid}）`, 'success');
        clearForm();
        await loadUsers();
      }
    } catch (e2) {
      setStatus(`送出失敗：${e2.message}`, 'error');
    }
  });

  async function deleteUser(uuid) {
    if (!uuid) return;
    const ok = confirm(`確定要刪除使用者？\nUUID: ${uuid}`);
    if (!ok) return;
    setStatus('刪除中...');
    try {
      await apiFetch(`${API_BASE}/${encodeURIComponent(uuid)}`, { method: 'DELETE' });
      setStatus('刪除成功。', 'success');
      await loadUsers();
    } catch (e) {
      setStatus(`刪除失敗：${e.message}`, 'error');
    }
  }

  // initial
  clearForm();
  loadUsers();
})();

