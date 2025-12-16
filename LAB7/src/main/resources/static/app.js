const API_BASE = 'http://localhost:8080/lab7-api';

// Глобальные переменные
let currentToken = null;
let currentUser = null;

// Показать/скрыть формы
function showSection(sectionId) {
    document.querySelectorAll('.auth-section').forEach(section => {
        section.classList.remove('active');
    });
    document.getElementById(sectionId).classList.add('active');
    document.getElementById('errorMsg').style.display = 'none';
}

function showLogin() { showSection('loginForm'); }
function showRegister() { showSection('registerForm'); }

// Показать сообщение
function showMessage(message, type = 'error') {
    const msgEl = document.getElementById('errorMsg');
    msgEl.textContent = message;
    msgEl.className = type === 'success' ? 'success' : 'error';
    msgEl.style.display = 'block';
    setTimeout(() => msgEl.style.display = 'none', 5000);
}

// Показать/скрыть лоадер
function setLoading(loading) {
    document.getElementById('loading').style.display = loading ? 'block' : 'none';
}

// LOGIN
async function login() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    if (!username || !password) {
        showMessage('Заполните все поля!');
        return;
    }

    setLoading(true);
    try {
        const response = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            currentToken = data.token;
            currentUser = data;
            showProfile();
            showMessage(`Добро пожаловать, ${data.username}! 🎉`, 'success');
        } else {
            showMessage(data.error || 'Ошибка входа');
        }
    } catch (error) {
        showMessage('Ошибка сети: ' + error.message);
    } finally {
        setLoading(false);
    }
}

// REGISTER
async function register() {
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;

    if (username.length < 3) {
        showMessage('Логин должен содержать минимум 3 символа');
        return;
    }
    if (password.length < 6) {
        showMessage('Пароль должен содержать минимум 6 символов');
        return;
    }

    setLoading(true);
    try {
        const response = await fetch(`${API_BASE}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok) {
            currentToken = data.token;
            currentUser = data;
            showProfile();
            showMessage(`Аккаунт создан, ${data.username}! 🎉`, 'success');
        } else {
            showMessage(data.error || 'Ошибка регистрации');
        }
    } catch (error) {
        showMessage('Ошибка сети: ' + error.message);
    } finally {
        setLoading(false);
    }
}

// ПОКАЗАТЬ ПРОФИЛЬ
function showProfile() {
    showSection('userProfile');
    document.getElementById('apiTest').style.display = 'block';

    document.getElementById('welcomeMsg').innerHTML =
        `✅ <strong>${currentUser.username}</strong> (${currentUser.role}) успешно авторизован!`;

    document.getElementById('userName').textContent = currentUser.username;
    document.getElementById('userRole').textContent = currentUser.role;
    document.getElementById('userId').textContent = currentUser.userId;
    document.getElementById('jwtToken').textContent = currentToken;
}

// КОПИРОВАТЬ ТОКЕН
function copyToken() {
    navigator.clipboard.writeText(currentToken).then(() => {
        showMessage('Токен скопирован в буфер!', 'success');
    });
}

// LOGOUT
function logout() {
    currentToken = null;
    currentUser = null;
    document.getElementById('apiTest').style.display = 'none';
    showLogin();
    showMessage('Вы вышли из системы 👋', 'success');
}

// ТЕСТ API
async function testApi(url, method = 'GET', body = null) {
    const apiResult = document.getElementById('apiResult');
    setLoading(true);

    try {
        const response = await fetch(`${API_BASE}${url}`, {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: body ? JSON.stringify(body) : null
        });

        const data = await response.json();

        apiResult.innerHTML = `
            <div class="success">
                ✅ <strong>${method} ${url}</strong><br>
                Status: <strong>${response.status}</strong><br>
                <pre>${JSON.stringify(data, null, 2)}</pre>
            </div>
        `;
    } catch (error) {
        apiResult.innerHTML = `
            <div class="error">
                ❌ Ошибка: ${error.message}
            </div>
        `;
    } finally {
        setLoading(false);
    }
}

// Тестовые кнопки
async function testUsersMe() {
    await testApi('/api/v1/users/me');
}

async function testUsersList() {
    await testApi('/api/v1/users');
}

async function testValidateToken() {
    await testApi('/api/auth/validate', 'GET');
}

// ENTER по формам
document.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        if (document.getElementById('loginForm').classList.contains('active')) {
            login();
        } else if (document.getElementById('registerForm').classList.contains('active')) {
            register();
        }
    }
});

// Автофокус
document.getElementById('loginUsername').focus();
