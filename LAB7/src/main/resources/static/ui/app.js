// src/main/resources/static/app.js

// Сохраняем токен в localStorage
function saveToken(token) {
    localStorage.setItem('token', token);
}

function getToken() {
    return localStorage.getItem('token');
}

function clearToken() {
    localStorage.removeItem('token');
}

// Универсальный запрос с токеном
async function apiRequest(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    const token = getToken();
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    const config = {
        ...options,
        headers
    };
    const res = await fetch(url, config);
    if (!res.ok) {
        const error = await res.json().catch(() => ({ error: 'Ошибка сервера' }));
        throw new Error(error.error || 'Неизвестная ошибка');
    }
    return res.json();
}

// === Вход ===
async function login() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');

    if (!username || !password) {
        errorDiv.textContent = 'Заполните все поля';
        return;
    }

    try {
        const data = await apiRequest('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        saveToken(data.token);
        window.location.href = 'profile.html';
    } catch (err) {
        errorDiv.textContent = err.message;
    }
}

// === Регистрация ===
async function register() {
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;
    const errorDiv = document.getElementById('registerError');

    if (!username || password.length < 6) {
        errorDiv.textContent = 'Пароль должен быть минимум 6 символов';
        return;
    }

    try {
        const data = await apiRequest('/api/auth/register', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        saveToken(data.token);
        window.location.href = 'profile.html';
    } catch (err) {
        errorDiv.textContent = err.message;
    }
}

// === Загрузка профиля ===
async function loadProfile() {
    try {
        const data = await apiRequest('/api/auth/me');
        document.getElementById('userId').textContent = data.id;
        document.getElementById('username').textContent = data.username;
        document.getElementById('role').textContent = data.role;
    } catch (err) {
        alert('Сессия истекла. Войдите снова.');
        clearToken();
        window.location.href = 'index.html';
    }
}

// === Выход ===
function logout() {
    clearToken();
    window.location.href = 'index.html';
}

// === Переключение форм ===
function showRegister() {
    document.getElementById('loginForm').classList.add('hidden');
    document.getElementById('registerForm').classList.remove('hidden');
}

function showLogin() {
    document.getElementById('registerForm').classList.add('hidden');
    document.getElementById('loginForm').classList.remove('hidden');
}