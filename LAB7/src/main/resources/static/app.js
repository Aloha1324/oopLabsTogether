const API_BASE = 'http://localhost:8080/lab7-api';

let currentToken = null;
let currentUser = null;
let factoryType = 'array';
let activeFuncA = null;
let activeFuncB = null;
let activeDiffFunc = null;
let currentChart = null;

let lastResultId = null;
let lastDiffResultId = null;

let hasUnsavedChanges = false;

// ===== NAVIGATION =====
function showSection(sectionId) {
    document.querySelectorAll('.auth-section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(sectionId).classList.add('active');
    document.getElementById('errorMsg').style.display = 'none';
}

function showLogin() { showSection('loginForm'); }
function showRegister() { showSection('registerForm'); }
function showProfile() { showSection('userProfile'); updateProfileUI(); }
function showCreateByPoints() { showSection('createByPoints'); }
function showCreateByFormula() { showSection('createByFormula'); loadMathFunctions(); }
function showFactorySettings() { showSection('factorySettings'); loadFactorySettings(); }
function showOperations() { showSection('operations'); }
function showDifferentiation() { showSection('differentiation'); }
function showFunctionViewer() { showSection('functionViewer'); loadFunctionsForViewer(); }

function updateProfileUI() {
    if (currentUser) {
        document.getElementById('welcomeMsg').innerHTML =
            `✅ <strong>${currentUser.username}</strong> (${currentUser.role}) успешно авторизован!`;
        document.getElementById('userName').textContent = currentUser.username;
        document.getElementById('userRole').textContent = currentUser.role;
        document.getElementById('userId').textContent = currentUser.userId;
    }
}

// ===== MESSAGES & LOADING =====
function showMessage(message, type = 'error') {
    const msgEl = document.getElementById('errorMsg');
    msgEl.textContent = message;
    msgEl.className = type === 'success' ? 'success' : 'error';
    msgEl.style.display = 'block';
    setTimeout(() => msgEl.style.display = 'none', 5000);
}

function showErrorModal(message) {
    document.getElementById('modalErrorMessage').textContent = message;
    document.getElementById('errorModal').style.display = 'flex';
    document.body.style.overflow = 'hidden'; // блокируем скролл
}

function closeErrorModal() {
    document.getElementById('errorModal').style.display = 'none';
    document.body.style.overflow = '';
}

function setLoading(loading) {
    document.getElementById('loading').style.display = loading ? 'block' : 'none';
}

// ===== AUTH =====
async function login() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    if (!username || !password) return showErrorModal('Заполните все поля!');
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (res.ok) {
            currentToken = data.token;
            currentUser = data;
            showProfile();
            wordleGame.updateFabVisibility();
            showMessage(`Добро пожаловать, ${data.username}! 🎉`, 'success');
        } else {
            showErrorModal(data.message || data.error || 'Ошибка входа');
        }
    } catch (err) {
        showErrorModal('Ошибка сети: ' + err.message);
    } finally {
        setLoading(false);
    }
}

async function register() {
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;
    if (username.length < 3) return showErrorModal('Логин должен содержать минимум 3 символа');
    if (password.length < 6) return showErrorModal('Пароль должен содержать минимум 6 символов');
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (res.ok) {
            currentToken = data.token;
            currentUser = data;
            showProfile();
            wordleGame.updateFabVisibility();
            showMessage(`Аккаунт создан, ${data.username}! 🎉`, 'success');
        } else {
            showErrorModal(data.message || data.error || 'Ошибка регистрации');
        }
    } catch (err) {
        showErrorModal('Ошибка сети: ' + err.message);
    } finally {
        setLoading(false);
    }
}



function logout() {
    currentToken = null;
    currentUser = null;
    activeFuncA = null;
    activeFuncB = null;
    activeDiffFunc = null;
    wordleGame.updateFabVisibility();

    showLogin();
    showMessage('Вы вышли из системы 👋', 'success');
}

// ===== MATH FUNCTIONS (FORMULA) =====
async function loadMathFunctions() {
    const select = document.getElementById('mathFunctionSelect');
    if (!select) {
        console.error("Элемент 'mathFunctionSelect' не найден!");
        return;
    }
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/tabulated/math-functions`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const functions = await res.json();
        console.log("Загруженные функции:", functions); // ← для отладки
        select.innerHTML = '';
        if (Array.isArray(functions) && functions.length > 0) {
            functions.forEach(f => {
                const opt = document.createElement('option');
                opt.value = f.key;
                opt.textContent = f.description;
                select.appendChild(opt);
            });
        } else {
            const opt = document.createElement('option');
            opt.value = '';
            opt.textContent = 'Нет функций';
            select.appendChild(opt);
        }
    } catch (err) {
        console.error("Ошибка загрузки функций:", err);
        showMessage('Не удалось загрузить функции: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// ===== CREATE BY POINTS =====
function generatePointsTable() {
    const count = parseInt(document.getElementById('pointsCount').value) || 0;
      if (count > 10000) {
        showErrorModal('Максимальное количество точек — 10 000');
        return;
      }
      if (count < 2) {
        showErrorModal('Минимум 2 точки');
        return;
      }

    const container = document.getElementById('pointsTableContainer');
    const hasData = container.querySelector('input') &&
                  Array.from(container.querySelectorAll('input')).some(inp => inp.value !== '');
    if (hasData) {
        if (!confirm('Текущие данные будут потеряны. Продолжить?')) return;
    }
    const countEl = document.getElementById('pointsCount');
    if (count < 2 || count > 10000) {
        container.innerHTML = '<div class="error" style="padding:8px;">Введите число от 2 до 10000</div>';
        return;
    }
    let html = `<table><thead><tr><th>x</th><th>y</th></tr></thead><tbody>`;
    for (let i = 0; i < count; i++) {
        html += `
            <tr>
                <td><input type="number" step="0.01" id="x_${i}" placeholder="x" style="width:100%;"></td>
                <td><input type="number" step="0.01" id="y_${i}" placeholder="y" style="width:100%;"></td>
            </tr>`;
    }
    html += `</tbody></table>`;
    container.innerHTML = html;
}

async function createFunctionFromPoints() {
    const name = document.getElementById('pointsName').value || null;
    const count = parseInt(document.getElementById('pointsCount').value) || 0;
    if (count < 2) return showErrorModal('Укажите ≥2 точки');
    const xVals = [], yVals = [];
    for (let i = 0; i < count; i++) {
        const xInput = document.getElementById(`x_${i}`).value;
        const yInput = document.getElementById(`y_${i}`).value;
        if (xInput === '' || yInput === '') return showErrorModal(`Ошибка в строке ${i + 1}: введите числа`);
        const x = parseFloat(xInput);
        const y = parseFloat(yInput);
        if (isNaN(x) || isNaN(y)) return showErrorModal(`Ошибка в строке ${i + 1}: введите корректные числа`);
        xVals.push(x); // просто число
        yVals.push(y);
    }
    for (let i = 1; i < xVals.length; i++) {
        if (xVals[i] <= xVals[i - 1]) return showErrorModal('x должны строго возрастать!');
    }
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/tabulated/by-points`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ name, xValues: xVals, yValues: yVals })
        });
        const data = await res.json();
        if (res.ok) {
            showMessage('Функция создана! 🎉', 'success');
            showProfile();
        } else {
            showErrorModal(data.error || data.message || 'Ошибка создания');
        }
    } catch (err) {
        showErrorModal('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// ===== CREATE BY FORMULA =====
async function createFunctionFromMath() {
    const name = document.getElementById('formulaName').value || null;
    const type = document.getElementById('mathFunctionSelect').value;
    const fromX = parseFloat(document.getElementById('fromX').value);
    const toX = parseFloat(document.getElementById('toX').value);
    const count = parseInt(document.getElementById('formulaPointsCount').value);
    if (!type) return showErrorModal('Выберите функцию');
    if (isNaN(fromX) || isNaN(toX)) return showErrorModal('Введите корректный интервал');
    if (fromX >= toX) return showErrorModal('Левая граница < правой');
    if (count < 2 || count > 10000) return showErrorModal('Точек от 2 до 10000');
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/tabulated/by-math-function`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ name, mathFunctionType: type, fromX, toX, pointsCount: count })
        });
        const data = await res.json();
        if (res.ok) {
            showMessage('Функция создана! 🎉', 'success');
            showProfile();
        } else {
            showErrorModal(data.error || data.message || 'Ошибка создания');
        }
    } catch (err) {
        showErrorModal('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

async function loadFile(input, target) {
    const file = input.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/import`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${currentToken}` },
            body: formData
        });
        const func = await res.json();
        if (res.ok) {
            if (target === 'A') {
                activeFuncA = func;
                renderEditableTable(func, 'funcATable', 'A');
            } else if (target === 'B') {
                activeFuncB = func;
                renderEditableTable(func, 'funcBTable', 'B');
            } else if (target === 'DIFF') {
                activeDiffFunc = func;
                renderEditableTable(func, 'diffInputTable', 'DIFF');
            }
            showMessage('Функция загружена из файла!', 'success');
        } else {
            showErrorModal('Ошибка загрузки из файла');
        }
    } catch (err) {
        showErrorModal('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
        input.value = ''; // reset
    }
}

// ===== FACTORY SETTINGS =====
async function loadFactorySettings() {
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/factory`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (res.ok) {
            const data = await res.json();
            factoryType = data.type;
            document.getElementById('currentFactory').textContent =
                factoryType === 'array' ? 'Массив' : 'Связный список';
            document.querySelector(`input[name="factory"][value="${factoryType}"]`).checked = true;
        }
    } catch (err) {
        showErrorModal('Не удалось загрузить настройки: ' + err.message);
    } finally {
        setLoading(false);
    }
}

async function saveFactorySettings() {
    const selected = document.querySelector('input[name="factory"]:checked')?.value;
    if (!selected) return showErrorModal('Выберите тип фабрики');
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/factory`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ type: selected })
        });
        if (res.ok) {
            factoryType = selected;
            showMessage('Фабрика сохранена!', 'success');
            showProfile();
        } else {
            const err = await res.json();
            showErrorModal(err.message || 'Ошибка сохранения');
        }
    } catch (err) {
        showErrorModal('Ошибка сети: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// ===== OPERATIONS =====
function createFuncForOp(target, type) {
    const originalBack = () => showSection('operations');
    if (type === 'points') {
        showCreateByPoints = () => {
            showSection('createByPoints');
            const backButton = document.querySelector('#createByPoints .btn-danger');
            if (backButton) backButton.onclick = originalBack;
        };
        showCreateByPoints();
    } else {
        showCreateByFormula = () => {
            showSection('createByFormula');
            const backButton = document.querySelector('#createByFormula .btn-danger');
            if (backButton) backButton.onclick = originalBack;
        };
        showCreateByFormula();
    }
}

async function performOp(operation) {
    if (!activeFuncA || !activeFuncB) return showErrorModal('Загрузите обе функции!');
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/operations/${operation}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({
                functionAId: activeFuncA.id,
                functionBId: activeFuncB.id

            })
        });
        const data = await res.json();
        if (res.ok) {
            renderReadOnlyTable(data, 'resultTable');
            lastResultId = data.id;
            showMessage('Операция выполнена!', 'success');
        } else {

            showErrorModal(data.message || 'На ноль делить нельзя ;)');
        }
    } catch (err) {
        showErrorModal('Ошибка сети: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// ===== DIFFERENTIATION =====
async function performDifferentiation() {
    if (!activeDiffFunc) return showErrorModal('Загрузите функцию для дифференцирования!');
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/differentiate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({
                functionId: activeDiffFunc.id,
                factoryType: factoryType
            })
        });
        const data = await res.json();
        if (res.ok) {
            renderReadOnlyTable(data, 'diffResultTable');
            lastDiffResultId = data.id;
            showMessage('Дифференцирование выполнено!', 'success');
        } else {
            showErrorModal(data.error || data.message || 'Ошибка дифференцирования');
        }
    } catch (err) {
        showErrorModal('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// ===== UTILITIES =====
// Для операндов (редактируемый Y)
function renderEditableTable(func, containerId, target) {
    const container = document.getElementById(containerId);
    if (!func || !func.points || func.points.length === 0) {
        container.innerHTML = '<div>Нет данных</div>';
        return;
    }
    let html = `<table><thead><tr><th>x</th><th>y</th></tr></thead><tbody>`;
    func.points.forEach((p, i) => {
        html += `
            <tr>
                <td>${Number(p.x).toFixed(4)}</td>
                <td>
                    <input type="number" step="0.01"
                           value="${Number(p.y).toFixed(4)}"
                           oninput="updateY('${target}', ${i}, this.value)">
                </td>
            </tr>`;
    });
    html += `</tbody></table>`;
    container.innerHTML = html;
}

// Для результата (только чтение)
function renderReadOnlyTable(func, containerId) {
    const container = document.getElementById(containerId);
    if (!func || !func.points || func.points.length === 0) {
        container.innerHTML = '<div>Нет данных</div>';
        return;
    }
    let html = `<table><thead><tr><th>x</th><th>y</th></tr></thead><tbody>`;
    func.points.forEach(p => {
        html += `<tr><td>${Number(p.x).toFixed(4)}</td><td>${Number(p.y).toFixed(4)}</td></tr>`;
    });
    html += `</tbody></table>`;
    container.innerHTML = html;
}

async function updateY(target, index, value) {
    let func;
    if (target === 'A') func = activeFuncA;
    else if (target === 'B') func = activeFuncB;
    else if (target === 'DIFF') func = activeDiffFunc;
    else return;

    if (!func || !func.points || index >= func.points.length || func.id == null) {
        console.warn('Невозможно обновить: функция не загружена или нет ID');
        return;
    }

    const oldY = func.points[index].y;
    const newY = parseFloat(value);

    if (isNaN(newY)) {
        func.points[index].y = oldY; // откат
        return;
    }

    // 1. Обновляем локально (для UI)
    func.points[index].y = newY;

    // 2. Отправляем на сервер
    try {
        const response = await fetch(`${API_BASE}/api/v1/functions/${func.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({
                points: func.points.map(p => ({
                    x: p.x,
                    y: p.y
                }))
                // Остальные поля (name, type и т.д.) не обязательны при обновлении точек
            })
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP ${response.status}`);
        }

        console.log(`✅ Точка ${index} обновлена: ${oldY} → ${newY}`);
    } catch (error) {
        // ❌ При ошибке — откатываем локальные изменения
        func.points[index].y = oldY;
        showErrorModal(`Ошибка обновления точки: ${error.message}`);
    }
}


function saveResult() {
    if (!activeFuncA || !activeFuncB) return showErrorModal('Нет данных для сохранения');
    // Предположим, что performOp сохраняет ID результата в lastResultId
    if (typeof lastResultId === 'number') {
        const a = document.createElement('a');
        a.href = `${API_BASE}/api/v1/functions/${lastResultId}/export`;
        a.download = `result_${lastResultId}.bin`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
    } else {
        showErrorModal('Сначала выполните операцию');
    }
}

function saveDiffResult() {
    if (!activeDiffFunc) return showErrorModal('Нет данных для сохранения');
    if (typeof lastDiffResultId === 'number') {
        const a = document.createElement('a');
        a.href = `${API_BASE}/api/v1/functions/${lastDiffResultId}/export`;
        a.download = `derivative_${lastDiffResultId}.bin`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
    } else {
        showErrorModal('Сначала выполните дифференцирование');
    }
}

async function loadFunction(target) {
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        const functions = await res.json();
        if (!res.ok) throw new Error('Не удалось загрузить функции');

        const funcId = prompt('Введите ID функции:\n' +
            functions.map(f => `${f.id}: ${f.name}`).join('\n'));
        if (!funcId) return;

        const func = functions.find(f => f.id == funcId);
        if (!func) {
            showErrorModal('Функция не найдена');
            return;
        }

        if (target === 'A') {
            activeFuncA = func;
            renderEditableTable(func, 'funcATable', 'A');
        } else if (target === 'B') {
            activeFuncB = func;
            renderEditableTable(func, 'funcBTable', 'B');
        } else if (target === 'DIFF') {
            activeDiffFunc = func;
            renderEditableTable(func, 'diffInputTable', 'DIFF');
        }
        showMessage('Функция загружена!', 'success');
    } catch (err) {
        showErrorModal('Ошибка загрузки: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// ============== ГРАФИК ФУНКЦИИ ==============
async function loadFunctionsForViewer() {
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/my`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Не удалось загрузить функции');
        }
        const functions = await res.json();
        const select = document.getElementById('functionSelect');
        select.innerHTML = '<option value="">-- Выберите функцию --</option>';

        const sortedFunctions = (functions || []).sort((a, b) => {
            const nameA = (a?.name || '').toString();
            const nameB = (b?.name || '').toString();
            return nameA.localeCompare(nameB, 'ru');
        });

        sortedFunctions.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.id;
            opt.textContent = `${f.name} (${f.type})`;
            select.appendChild(opt);
        });
    } catch (err) {
        showErrorModal(err.message);
    } finally {
        setLoading(false);
    }
}

async function showFunctionSelector(target) {
    const selectId = target === 'A' ? 'funcASelector' :
                     target === 'B' ? 'funcBSelector' : 'diffFuncSelector';
    const select = document.getElementById(selectId);
    if (!select) return;

    // Показываем селект
    select.style.display = 'block';

    // Загружаем только функции текущего пользователя
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/my`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        const functions = await res.json();
        if (!res.ok) throw new Error('Не удалось загрузить функции');

        // Очищаем и заполняем
        select.innerHTML = '<option value="">-- Выберите функцию --</option>';
        functions.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.id;
            opt.textContent = `${f.name} (ID: ${f.id})`;
            select.appendChild(opt);
        });
    } catch (err) {
        showErrorModal('Ошибка загрузки: ' + err.message);
        select.style.display = 'none';
    } finally {
        setLoading(false);
    }
}

async function selectFunctionFromDropdown(target, funcId) {
    if (!funcId) return;
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/${funcId}`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        const func = await res.json();
        if (!res.ok) throw new Error('Функция не найдена');

        // ✅ Сохраняем флаги insertable/removable
        func.insertable = Boolean(func.insertable);
        func.removable = Boolean(func.removable);

        if (target === 'A') {
            activeFuncA = func;
            renderEditableTable(func, 'funcATable', 'A');
            updateControlButtons('A', func);
        } else if (target === 'B') {
            activeFuncB = func;
            renderEditableTable(func, 'funcBTable', 'B');
            updateControlButtons('B', func);
        } else if (target === 'DIFF') {
            activeDiffFunc = func;
            renderEditableTable(func, 'diffInputTable', 'DIFF');
            updateControlButtons('DIFF', func);
        }
        showMessage('Функция загружена!', 'success');
    } catch (err) {
        showErrorModal('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

function toggleFunctionSelector(target) {
    const selectId =
        target === 'A' ? 'funcASelector' :
        target === 'B' ? 'funcBSelector' : 'diffFuncSelector';
    const select = document.getElementById(selectId);
    if (!select) return;

    if (select.style.display === 'none') {
        // Загрузить список функций и показать
        loadUserFunctionsIntoSelect(target);
    } else {
        // Скрыть
        select.style.display = 'none';
    }
}

async function loadUserFunctionsIntoSelect(target) {
    const selectId =
        target === 'A' ? 'funcASelector' :
        target === 'B' ? 'funcBSelector' : 'diffFuncSelector';
    const select = document.getElementById(selectId);
    if (!select) return;

    setLoading(true);
    try {
        // ⚠️ ВАЖНО: используем НОВЫЙ эндпоинт, который возвращает ТОЛЬКО функции пользователя
        const res = await fetch(`${API_BASE}/api/v1/functions/my`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (!res.ok) throw new Error('Не удалось загрузить функции');
        const functions = await res.json();

        // Очистка и заполнение
        select.innerHTML = '<option value="">-- Выберите функцию --</option>';
        functions.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.id;
            opt.textContent = `${f.name || 'Без названия'} (ID: ${f.id})`;
            select.appendChild(opt);
        });

        // Показываем
        select.style.display = 'block';
    } catch (err) {
        showErrorModal('Ошибка загрузки функций: ' + err.message);
    } finally {
        setLoading(false);
    }
}



async function loadFunctionForGraph() {
    const id = document.getElementById('functionSelect').value;
    if (!id) {
        clearGraphAndTable();
        updateControlButtons('VIEWER', null); // скрыть кнопки
        return;
    }
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/${id}`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Функция не найдена');
        }
        const func = await res.json();
        // ✅ Сохраняем флаги
        func.insertable = Boolean(func.insertable);
        func.removable = Boolean(func.removable);

        renderFunctionGraph(func);
        renderFunctionTableForGraph(func, 'functionPointsTable');
        window.activeViewerFunc = func; // сохраним для кнопок
        updateControlButtons('VIEWER', func);
    } catch (err) {
        showErrorModal(err.message);
        clearGraphAndTable();
        updateControlButtons('VIEWER', null);
    } finally {
        setLoading(false);
    }
}

function clearGraphAndTable() {
    renderFunctionGraph(null);
    document.getElementById('functionPointsTable').innerHTML = '';
    document.getElementById('evalResult').style.display = 'none';
}

function renderFunctionGraph(func) {
    const ctx = document.getElementById('functionChart').getContext('2d');
    if (currentChart) {
        currentChart.destroy();
        currentChart = null;
    }
    if (!func || !func.points) return;

    const xData = func.points.map(p => p.x);
    const yData = func.points.map(p => p.y);

    currentChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: xData.map(x => x.toFixed(4)),
            datasets: [{
                label: func.name,
                data: yData,
                borderColor: '#f6ad55',
                backgroundColor: 'rgba(246, 173, 85, 0.1)',
                borderWidth: 2,
                fill: false,
                tension: 0.3,
                pointRadius: 4,
                pointBackgroundColor: '#fbbf24'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    title: { display: true, text: 'x', color: '#e2e8f0' },
                    grid: { color: 'rgba(255,255,255,0.1)' },
                    ticks: { color: '#a0aec0' }
                },
                y: {
                    title: { display: true, text: 'y', color: '#e2e8f0' },
                    grid: { color: 'rgba(255,255,255,0.1)' },
                    ticks: { color: '#a0aec0' }
                }
            },
            plugins: {
                legend: { labels: { color: '#e2e8f0' } },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                    callbacks: {
                        label: (context) => `y = ${parseFloat(context.parsed.y).toFixed(6)}`
                    }
                }
            },
            interaction: {
                mode: 'nearest',
                axis: 'x',
                intersect: false
            }
        }
    });
}

function renderFunctionTableForGraph(func, containerId) {
    const container = document.getElementById(containerId);
    if (!func || !func.points) {
        container.innerHTML = '';
        return;
    }
    let html = `<h3>Точки функции "${func.name}"</h3><table class="table"><thead><tr><th>x</th><th>y</th></tr></thead><tbody>`;
    func.points.forEach(p => {
        html += `<tr><td>${p.x.toFixed(6)}</td><td>${p.y.toFixed(6)}</td></tr>`;
    });
    html += `</tbody></table>`;
    container.innerHTML = html;
}

async function evaluateAtX() {
    const id = document.getElementById('functionSelect').value;
    const xInput = document.getElementById('evalX').value.trim();
    if (!id) return showErrorModal('Сначала выберите функцию');
    const x = parseFloat(xInput);
    if (isNaN(x)) return showErrorModal('Введите корректное число в поле x');

    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/operations/value`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ functionId: parseInt(id), x: x })
        });
        const result = await res.json();
        const evalResultEl = document.getElementById('evalResult');
        if (res.ok && result.success) {
            evalResultEl.textContent = `f(${x}) = ${result.result.toFixed(8)}`;
            evalResultEl.style.display = 'block';
        } else {
            throw new Error(result.message || 'Ошибка вычисления значения');
        }
    } catch (err) {
        showErrorModal(err.message);
        document.getElementById('evalResult').style.display = 'none';
    } finally {
        setLoading(false);
    }
}

function showCreateByPointsForViewer() {
    showCreateByPoints();
}

function showCreateByFormulaForViewer() {
    showCreateByFormula();
}

function updateControlButtons(target, func) {
    const prefix =
        target === 'A' ? 'funcA' :
        target === 'B' ? 'funcB' :
        target === 'DIFF' ? 'diff' :
        target === 'VIEWER' ? 'viewer' :
        '';
    const insertBtn = document.getElementById(`${prefix}InsertBtn`);
    const removeBtn = document.getElementById(`${prefix}RemoveBtn`);
    const insertPanel = document.getElementById(`${prefix}InsertPanel`);
    const removePanel = document.getElementById(`${prefix}RemovePanel`);
    const isVisible = func && func.insertable !== undefined && func.removable !== undefined;
    if (insertBtn) insertBtn.style.display = isVisible && func.insertable ? 'inline-block' : 'none';
    if (removeBtn) removeBtn.style.display = isVisible && func.removable ? 'inline-block' : 'none';
    if (insertPanel) insertPanel.style.display = isVisible && func.insertable ? 'flex' : 'none';
    if (removePanel) removePanel.style.display = isVisible && func.removable ? 'flex' : 'none'; // ← ИСПРАВЛЕНО!
}
async function insertPoint(target) {
    let func;
    let prefix;
    if (target === 'A') { func = activeFuncA; prefix = 'funcA'; }
    else if (target === 'B') { func = activeFuncB; prefix = 'funcB'; }
    else if (target === 'DIFF') { func = activeDiffFunc; prefix = 'diff'; }
    else if (target === 'VIEWER') { func = window.activeViewerFunc; prefix = 'viewer'; }
    else return;

    if (!func || !func.insertable) return showErrorModal('Функция не поддерживает вставку');

    const xInput = document.getElementById(`${prefix}InsertX`).value;
    const yInput = document.getElementById(`${prefix}InsertY`).value;
    const x = parseFloat(xInput), y = parseFloat(yInput);
    if (isNaN(x) || isNaN(y)) return showErrorModal('Введите корректные x и y');

    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/${func.id}/insert`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentToken}` },
            body: JSON.stringify({ x, y })
        });
        if (res.ok) {
            // Перезагружаем функцию
            if (target === 'VIEWER') {
                await loadFunctionForGraph();
            } else {
                await selectFunctionFromDropdown(target, func.id);
            }
            showMessage('Точка вставлена!', 'success');
        } else {
            const err = await res.json().catch(() => ({}));
            showErrorModal(err.message || 'Ошибка вставки');
        }
    } catch (err) {
        showErrorModal('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

async function removePoint(target) {
    let func;
    let prefix;
    if (target === 'A') { func = activeFuncA; prefix = 'funcA'; }
    else if (target === 'B') { func = activeFuncB; prefix = 'funcB'; }
    else if (target === 'DIFF') { func = activeDiffFunc; prefix = 'diff'; }
    else if (target === 'VIEWER') { func = window.activeViewerFunc; prefix = 'viewer'; }
    else return;

    if (!func || !func.removable) return showErrorModal('Функция не поддерживает удаление');

    const indexInput = document.getElementById(`${prefix}RemoveIndex`).value;
    const index = parseInt(indexInput);
    if (isNaN(index) || index < 0 || index >= func.points.length) {
        return showErrorModal(`Индекс от 0 до ${func.points.length - 1}`);
    }

    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/${func.id}/remove?index=${index}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (res.ok) {
            if (target === 'VIEWER') {
                await loadFunctionForGraph();
            } else {
                await selectFunctionFromDropdown(target, func.id);
            }
            showMessage('Точка удалена!', 'success');
        } else {
            const err = await res.json().catch(() => ({}));
            showErrorModal(err.message || 'Ошибка удаления');
        }
    } catch (err) {
        showErrorModal('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// Загрузка списка функций для интеграла
async function loadFunctionsForIntegral() {
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/my`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (!res.ok) throw new Error('Ошибка загрузки функций');
        const functions = await res.json();
        const select = document.getElementById('integralFunctionSelect');
        select.innerHTML = '<option value="">-- Выберите функцию --</option>';
        functions.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.id;
            opt.textContent = f.name;
            select.appendChild(opt);
        });
    } catch (err) {
        showErrorModal('Ошибка загрузки функций: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// Вычисление интеграла
async function calculateIntegral() {
    const funcId = document.getElementById('integralFunctionSelect').value;
    const threadsInput = document.getElementById('integralThreads').value;
    const threads = parseInt(threadsInput);

    if (!funcId) return showErrorModal('Выберите функцию');
    if (isNaN(threads) || threads < 1 || threads > 16) {
        return showErrorModal('Количество потоков должно быть от 1 до 16');
    }

    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/integrate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ functionId: parseInt(funcId), threadCount: threads })
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Ошибка вычисления интеграла');
        }

        const result = await res.json();
        document.getElementById('integralResult').innerHTML = `
            <div class="success">
                <strong>Результат интеграла:</strong> ${result.result.toFixed(8)}<br>
                <strong>Время выполнения:</strong> ${result.calculationTime} мс<br>
                <strong>Использовано потоков:</strong> ${threads}
            </div>
        `;
    } catch (err) {
        showErrorModal('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// Навигация: показать окно интеграла
function showIntegralViewer() {
    showSection('integralViewer');
    loadFunctionsForIntegral();
}

// Загрузка функций для Composite
async function loadFunctionsForComposite() {
    const res = await fetch(`${API_BASE}/api/v1/functions/my`, {
        headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const functions = await res.json();
    ['compositeFuncA', 'compositeFuncB'].forEach(id => {
        const select = document.getElementById(id);
        select.innerHTML = '<option value="">-- Выберите --</option>';
        functions.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.id;
            opt.textContent = f.name;
            select.appendChild(opt);
        });
    });
}

// Создание сложной функции
async function createCompositeFunction() {
    const name = document.getElementById('compositeName').value.trim();
    const funcA = document.getElementById('compositeFuncA').value;
    const funcB = document.getElementById('compositeFuncB').value;
    const operation = document.getElementById('compositeOperation').value;

    if (!name) return showErrorModal('Введите название');
    if (!funcA || !funcB) return showErrorModal('Выберите обе функции');

    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/composite`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({
                functionAId: parseInt(funcA),
                functionBId: parseInt(funcB),
                operation: operation,
                name: name
            })
        });
        if (res.ok) {
            showMessage('Сложная функция создана!', 'success');
            setTimeout(showProfile, 1000);
        } else {
            const err = await res.json().catch(() => ({}));
            showErrorModal(err.message || 'Ошибка создания');
        }
    } catch (err) {
        showErrorModal('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// Навигация
function showCompositeCreator() {
    showSection('compositeCreator');
    loadFunctionsForComposite();
}
// Сохранение результата как JSON
function saveResultAsJson() {
    if (typeof lastResultId !== 'number') {
        showErrorModal('Сначала выполните операцию');
        return;
    }
    const a = document.createElement('a');
    a.href = `${API_BASE}/api/v1/functions/${lastResultId}/export/json`;
    a.download = `result_${lastResultId}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
}

// Загрузка результата из JSON
function loadResultFromJson() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const text = await file.text();
        setLoading(true);
        try {
            const res = await fetch(`${API_BASE}/api/v1/functions/import/json`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentToken}` },
                body: text
            });
            const func = await res.json();
            if (res.ok) {
                showMessage('Функция загружена из JSON!', 'success');
                activeFuncA = func; // можно также загрузить в B — зависит от логики
                renderEditableTable(func, 'funcATable', 'A');
            } else {
                showErrorModal('Ошибка загрузки JSON');
            }
        } catch (err) {
            showErrorModal('Ошибка: ' + err.message);
        } finally {
            setLoading(false);
        }
    };
    input.click();
}
function resetGlobalState() {
    activeFuncA = null;
    activeFuncB = null;
    activeDiffFunc = null;
    // ЗАДАНИЕ 3: сброс данных просмотрщика
    currentViewFunction = null;
    currentIntegralFunction = null;
    if (functionChart) {
        functionChart.destroy();
        functionChart = null;
    }
}

// Сохранение результата дифференцирования как JSON
function saveDiffResultAsJson() {
    if (typeof lastDiffResultId !== 'number') {
        showErrorModal('Сначала выполните дифференцирование');
        return;
    }
    const a = document.createElement('a');
    a.href = `${API_BASE}/api/v1/functions/${lastDiffResultId}/export/json`;
    a.download = `derivative_${lastDiffResultId}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
}

// Загрузка функции для дифференцирования из JSON
function loadDiffResultFromJson() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const text = await file.text();
        setLoading(true);
        try {
            const res = await fetch(`${API_BASE}/api/v1/functions/import/json`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentToken}` },
                body: text
            });
            const func = await res.json();
            if (res.ok) {
                showMessage('Функция загружена из JSON!', 'success');
                activeDiffFunc = func;
                renderEditableTable(func, 'diffInputTable', 'DIFF');
            } else {
                showErrorModal('Ошибка загрузки JSON');
            }
        } catch (err) {
            showErrorModal('Ошибка: ' + err.message);
        } finally {
            setLoading(false);
        }
    };
    input.click();
}

// Экспорт текущей функции как JSON
function exportFunctionAsJson() {
    const funcId = document.getElementById('functionSelect').value;
    if (!funcId) {
        showErrorModal('Выберите функцию');
        return;
    }
    const a = document.createElement('a');
    a.href = `${API_BASE}/api/v1/functions/${funcId}/export/json`;
    a.download = `function_${funcId}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
}

// Импорт функции из JSON в график
function importFunctionFromJsonFile() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const text = await file.text();
        setLoading(true);
        try {
            const res = await fetch(`${API_BASE}/api/v1/functions/import/json`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${currentToken}` },
                body: text
            });
            const func = await res.json();
            if (res.ok) {
                showMessage('Функция импортирована из JSON!', 'success');
                // Обновляем список функций и автоматически выбираем новую
                await loadFunctionForGraph(); // или просто обновить список
                loadFunctionsForViewer(); // обновить выпадающий список
                document.getElementById('functionSelect').value = func.id;
                await loadFunctionForGraph();
            } else {
                showErrorModal('Ошибка импорта JSON');
            }
        } catch (err) {
            showErrorModal('Ошибка: ' + err.message);
        } finally {
            setLoading(false);
        }
    };
    input.click();
}


// ===== WORDLE GAME CLASS =====
class WordleGame {
    constructor() {
        this.wordleOpen = false;
        this.wordleGame = null;
        this.gameState = null;
        this.guesses = [];
        this.currentGuess = [];
        this.gameOver = false;
        this.init();
        this.messageTimeout = null;
    }

    init() {
        this.createGameDOM();
        this.setupEventListeners();
        this.updateFabVisibility();
    }

    createGameDOM() {
        this.wordleGame = document.createElement('div');
        this.wordleGame.id = 'wordleGame';
        this.wordleGame.innerHTML = `
            <div class="wordle-overlay" onclick="wordleGame.close()">
                <div class="wordle-container" onclick="event.stopPropagation()">
                    <div class="wordle-header">
                        <h3>🟩🟨⬜ WORDLE</h3>
                        <button class="wordle-close" onclick="wordleGame.close()">✕</button>
                    </div>
                    <div class="wordle-game">
                        <div id="wordleGrid"></div>
                        <div class="wordle-keyboard"></div>
                        <div class="wordle-info">
                            <div>Осталось попыток: <span id="wordleAttempts">6</span></div>
                            <div id="wordleMessage"></div>
                            <div>
                                <button class="btn" style="width:100%; margin-top:10px;" onclick="wordleGame.newGame()">🔄 Новая игра</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(this.wordleGame);
        this.setupKeyboard();
    }

    setupEventListeners() {
        document.addEventListener('keydown', (e) => {
            if (!this.wordleOpen || this.gameOver) return;
            if (e.key === 'Backspace') {
                e.preventDefault();
                this.handleKeyPress('⌫');
            } else if (e.key === 'Enter') {
                e.preventDefault();
                this.handleKeyPress('.');
            } else if (e.key.length === 1 && /[А-ЯЁ]/.test(e.key.toUpperCase())) {
                this.handleKeyPress(e.key.toUpperCase());
            }
        });
    }

    setupKeyboard() {
        const keyboard = this.wordleGame.querySelector('.wordle-keyboard');
        const keys = [
            ['Й', 'Ц', 'У', 'К', 'Е', 'Ё', 'Н', 'Г', 'Ш', 'Щ', 'З', 'Х', 'Ъ'],
            ['Ф', 'Ы', 'В', 'А', 'П', 'Р', 'О', 'Л', 'Д', 'Ж', 'Э'],
            ['Я', 'Ч', 'С', 'М', 'И', 'Т', 'Ь', 'Б', 'Ю', '.', '⌫']
        ];

        keys.forEach(row => {
            const rowDiv = document.createElement('div');
            rowDiv.className = 'wordle-key-row';
            row.forEach(key => {
                const btn = document.createElement('button');
                btn.className = 'wordle-key';
                btn.textContent = key;
                btn.onclick = () => this.handleKeyPress(key);
                rowDiv.appendChild(btn);
            });
            keyboard.appendChild(rowDiv);
        });
    }

    async toggle() {
        if (!currentUser) {
            showErrorModal('⚠️ Сначала авторизуйтесь!', 'error');
            return;
        }
        this.wordleOpen ? this.close() : await this.open();
    }

    async open() {
        this.wordleGame.classList.add('active');
        this.wordleOpen = true;
        document.body.style.overflow = 'hidden';
        document.getElementById('wordleFabContainer').classList.add('active');
        await this.newGame();
    }

    close() {
        this.wordleGame.classList.remove('active');
        this.wordleOpen = false;
        document.body.style.overflow = '';
        document.getElementById('wordleFabContainer')?.classList.remove('active');
        this.gameOver = false;
    }

    async newGame() {
        try {
            setLoading(true);
            const res = await fetch(`${API_BASE}/api/v1/wordle/new-game`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${currentToken}` }
            });
            if (res.ok) {
                this.gameState = await res.json();
                this.guesses = [];
                this.currentGuess = [];
                this.gameOver = false;
                this.updateGrid();
                this.updateAttempts();
                this.clearMessage();
                showMessage('🆕 Новая игра начата!', 'success');
            } else {
                const err = await res.json();
                showErrorModal(err.message || 'Ошибка создания игры');
            }
        } catch (err) {
            showErrorModal('Ошибка сети: ' + err.message);
        } finally {
            setLoading(false);
        }
    }

    async loadGameState() {
        try {
            const res = await fetch(`${API_BASE}/api/v1/wordle/state`, {
                headers: { 'Authorization': `Bearer ${currentToken}` }
            });
            if (res.ok && res.status !== 204) {
                this.gameState = await res.json();
                this.updateAttempts();
            } else {
                await this.newGame();
            }
        } catch (err) {
            await this.newGame();
        }
    }

    async submitGuess() {
        if (this.currentGuess.length !== 5) {
            this.showMessage('Введите 5 букв!', 'error');
            return;
        }
        const guessWord = this.currentGuess.join('').toUpperCase();
        const alreadyGuessed = this.guesses.some(g => g.word === guessWord);
        if (alreadyGuessed) {
            this.showMessage('Это слово уже было использовано!', 'error');
            return;
        }

        try {
            setLoading(true);
            const res = await fetch(`${API_BASE}/api/v1/wordle/guess`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${currentToken}`
                },
                body: JSON.stringify({ word: guessWord })
            });
            const result = await res.json();
            await this.loadGameState();

            if (result.won || (result.message && result.message.includes('🎉'))) {
                this.guesses.push({ word: guessWord, status: result.status });
                this.gameOver = true;
                this.showMessage(result.message || '🎉 Победа!', 'success');
            } else if (result.message) {
                this.showMessage(result.message, 'error');
            } else {
                this.guesses.push({ word: guessWord, status: result.status });
                this.currentGuess = [];
                this.updateGrid();
                this.updateAttempts();
                if (this.gameState && this.gameState.attemptsLeft <= 0) {
                    this.gameOver = true;
                    this.showErrorModal(`Игра окончена! Слово: ${this.gameState.targetWord}`, 'error');
                }
            }
        } catch (err) {
            showMessage('Ошибка сети: ' + err.message);
        } finally {
            setLoading(false);
        }
    }

    handleKeyPress(key) {
        if (this.gameOver) return;

        if (key === '⌫') {
            this.currentGuess.pop();
        } else if (key === '.') {
            this.submitGuess();
        } else if (key.length === 1 && this.currentGuess.length < 5) {
            this.currentGuess.push(key);
        }

        this.updateGrid();
    }

    updateGrid() {
        const grid = this.wordleGame.querySelector('#wordleGrid');
        if (!grid) return;
        grid.innerHTML = '';

        for (let i = 0; i < 6; i++) {
            const row = document.createElement('div');
            row.className = 'wordle-row';

            for (let j = 0; j < 5; j++) {
                const cell = document.createElement('div');
                cell.className = 'wordle-cell';

                if (i < this.guesses.length) {
                    const guess = this.guesses[i];
                    if (j < guess.word.length && guess.status) {
                        cell.textContent = guess.word[j];
                        cell.className = `wordle-cell ${guess.status[j]}`;
                    }
                } else if (i === this.guesses.length && j < this.currentGuess.length) {
                    cell.textContent = this.currentGuess[j];
                    cell.className = 'wordle-cell current';
                }

                row.appendChild(cell);
            }
            grid.appendChild(row);
        }
    }

    updateAttempts() {
        const attemptsEl = this.wordleGame.querySelector('#wordleAttempts');
        if (attemptsEl) {
            attemptsEl.textContent = this.gameState ? this.gameState.attemptsLeft : 6;
        }
    }


    showMessage(msg, type) {
        const msgEl = this.wordleGame.querySelector('#wordleMessage');
        if (msgEl) {
            msgEl.textContent = msg;
            msgEl.className = type || '';

            if (type === 'error') {
                clearTimeout(this.messageTimeout);
                this.messageTimeout = setTimeout(() => {
                    if (msgEl.textContent === msg) {
                        this.clearMessage();
                    }
                }, 2500);
            }
        }
    }

    clearMessage() {
        const msgEl = this.wordleGame.querySelector('#wordleMessage');
        if (msgEl) {
            msgEl.textContent = '';
            msgEl.className = '';
        }
        if (this.messageTimeout) {
            clearTimeout(this.messageTimeout);
            this.messageTimeout = null;
        }
    }

    updateFabVisibility() {
        const fabContainer = document.getElementById('wordleFabContainer');
        if (fabContainer) {
            if (currentUser) {
                fabContainer.style.display = 'flex';
            } else {
                fabContainer.style.display = 'none';
                this.close();
            }
        }
    }
}

const wordleGame = new WordleGame();

// ===== KEYBOARD & INIT =====
document.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        if (document.getElementById('loginForm').classList.contains('active')) login();
        else if (document.getElementById('registerForm').classList.contains('active')) register();
    }
});

document.getElementById('loginUsername').focus();