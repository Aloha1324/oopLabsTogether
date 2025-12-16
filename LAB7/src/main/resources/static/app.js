const API_BASE = 'http://localhost:8080/lab7-api';

let currentToken = null;
let currentUser = null;

function showSection(sectionId) {
    document.querySelectorAll('.auth-section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(sectionId).classList.add('active');
    document.getElementById('errorMsg').style.display = 'none';
}

function showLogin() { showSection('loginForm'); }
function showRegister() { showSection('registerForm'); }
function showProfile() {
    showSection('userProfile');
    if (currentUser) {
        document.getElementById('welcomeMsg').innerHTML =
            `✅ <strong>${currentUser.username}</strong> (${currentUser.role}) успешно авторизован!`;
        document.getElementById('userName').textContent = currentUser.username;
        document.getElementById('userRole').textContent = currentUser.role;
        document.getElementById('userId').textContent = currentUser.userId;
        document.getElementById('jwtToken').textContent = currentToken;
    }
}

function showCreateByPoints() { showSection('createByPoints'); }
function showCreateByFormula() {
    showSection('createByFormula');
    loadMathFunctions();
}

function showMessage(message, type = 'error') {
    const msgEl = document.getElementById('errorMsg');
    msgEl.textContent = message;
    msgEl.className = type === 'success' ? 'success' : 'error';
    msgEl.style.display = 'block';
    setTimeout(() => msgEl.style.display = 'none', 5000);
}

function setLoading(loading) {
    document.getElementById('loading').style.display = loading ? 'block' : 'none';
}

// AUTH
async function login() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    if (!username || !password) return showMessage('Заполните все поля!');
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
            showMessage(`Добро пожаловать, ${data.username}! 🎉`, 'success');
        } else {
            showMessage(data.message || data.error || 'Ошибка входа');
        }
    } catch (err) {
        showMessage('Ошибка сети: ' + err.message);
    } finally {
        setLoading(false);
    }
}

async function register() {
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;
    if (username.length < 3) return showMessage('Логин должен содержать минимум 3 символа');
    if (password.length < 6) return showMessage('Пароль должен содержать минимум 6 символов');
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
            showMessage(`Аккаунт создан, ${data.username}! 🎉`, 'success');
        } else {
            showMessage(data.message || data.error || 'Ошибка регистрации');
        }
    } catch (err) {
        showMessage('Ошибка сети: ' + err.message);
    } finally {
        setLoading(false);
    }
}

function copyToken() {
    navigator.clipboard.writeText(currentToken).then(() => {
        showMessage('Токен скопирован!', 'success');
    });
}

function logout() {
    currentToken = null;
    currentUser = null;
    showLogin();
    showMessage('Вы вышли из системы 👋', 'success');
}

// NEW: MATH FUNCTIONS
async function loadMathFunctions() {
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/v1/functions/tabulated/math-functions`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        const functions = await res.json();
        const select = document.getElementById('mathFunctionSelect');
        select.innerHTML = '';
        functions.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.key;
            opt.textContent = f.description;
            select.appendChild(opt);
        });
    } catch (err) {
        showMessage('Не удалось загрузить функции: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// NEW: BY POINTS
function generatePointsTable() {
    const countEl = document.getElementById('pointsCount');
    const container = document.getElementById('pointsTableContainer');
    const count = parseInt(countEl.value) || 0;
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
    if (count < 2) return showMessage('Укажите ≥2 точки');
    const xVals = [], yVals = [];
    for (let i = 0; i < count; i++) {
        const x = parseFloat(document.getElementById(`x_${i}`).value);
        const y = parseFloat(document.getElementById(`y_${i}`).value);
        if (isNaN(x) || isNaN(y)) return showMessage(`Ошибка в строке ${i + 1}: введите числа`);
        xVals.push(x);
        yVals.push(y);
    }
    for (let i = 1; i < xVals.length; i++) {
        if (xVals[i] <= xVals[i - 1]) return showMessage('x должны строго возрастать!');
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
            showMessage(data.message || 'Ошибка создания');
        }
    } catch (err) {
        showMessage('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// NEW: BY MATH
async function createFunctionFromMath() {
    const name = document.getElementById('formulaName').value || null;
    const type = document.getElementById('mathFunctionSelect').value;
    const fromX = parseFloat(document.getElementById('fromX').value);
    const toX = parseFloat(document.getElementById('toX').value);
    const count = parseInt(document.getElementById('formulaPointsCount').value);
    if (!type) return showMessage('Выберите функцию');
    if (isNaN(fromX) || isNaN(toX)) return showMessage('Введите корректный интервал');
    if (fromX >= toX) return showMessage('Левая граница < правой');
    if (count < 2 || count > 10000) return showMessage('Точек от 2 до 10000');
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
            showMessage(data.message || 'Ошибка создания');
        }
    } catch (err) {
        showMessage('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// ENTER handling
document.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        if (document.getElementById('loginForm').classList.contains('active')) login();
        else if (document.getElementById('registerForm').classList.contains('active')) register();
    }
});

// Auto-focus
document.getElementById('loginUsername').focus();