// src/main/resources/static/app.js

let currentUser = null;
let selectedMathFunction = null;

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

/**
 * Загрузка профиля пользователя
 */
async function loadProfile() {
    try {
        const response = await fetch('/api/auth/profile', {
            credentials: 'include'
        });

        if (response.ok) {
            const user = await response.json();
            currentUser = user;
            document.getElementById('userId').textContent = user.id;
            document.getElementById('username').textContent = user.username;
            document.getElementById('role').textContent = user.role;
        } else {
            window.location.href = '/login.html';
        }
    } catch (error) {
        console.error('Ошибка загрузки профиля:', error);
        window.location.href = '/login.html';
    }
}

/**
 * Выход из аккаунта
 */
async function logout() {
    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include'
        });
        window.location.href = '/login.html';
    } catch (error) {
        console.error('Ошибка выхода:', error);
        window.location.href = '/login.html';
    }
}

// ============================================================================


/**
 * Открыть модалку "По точкам"
 */
window.openPointsModal = function() {
    document.getElementById('pointsModal').style.display = 'block';
    document.getElementById('pointsName').focus();
};

/**
 * Закрыть модалку "По точкам"
 */
window.closePointsModal = function() {
    document.getElementById('pointsModal').style.display = 'none';
    // Очищаем форму
    document.getElementById('pointsName').value = '';
    document.getElementById('pointsX').value = '';
    document.getElementById('pointsY').value = '';
};

/**
 * Открыть модалку "По формуле"
 */
window.openMathModal = function() {
    document.getElementById('mathModal').style.display = 'block';
    loadMathFunctions();
    document.getElementById('mathName').focus();
};

/**
 * Закрыть модалку "По формуле"
 */
window.closeMathModal = function() {
    document.getElementById('mathModal').style.display = 'none';
    document.getElementById('mathName').value = '';
    selectedMathFunction = null;
    // Снимаем подсветку
    document.querySelectorAll('.math-function').forEach(el => {
        el.style.background = '';
        el.style.color = '';
    });
};

/**
 * ВЫБОР ТИПА ФУНКЦИИ
 */
window.showCreateModal = function() {
    if (confirm('Что создать?\n\n• OK = По точкам\n• Cancel = По формуле')) {
        openPointsModal();
    } else {
        openMathModal();
    }
};

/**
 * Загрузка списка математических функций
 */
async function loadMathFunctions() {
    try {
        const response = await fetch('/api/v1/functions/tabulated/math-functions');
        const functions = await response.json();

        const container = document.getElementById('mathFunctionsList');
        container.innerHTML = '';

        functions.forEach(func => {
            const div = document.createElement('div');
            div.className = 'math-function';
            div.innerHTML = `<strong>${func.key}</strong><br><small>${func.description}</small>`;
            div.onclick = () => selectMathFunction(func.key, div);
            container.appendChild(div);
        });
    } catch (error) {
        console.error('Ошибка загрузки математических функций:', error);
        document.getElementById('mathFunctionsList').innerHTML =
            '<div style="color: var(--error-red);">Ошибка загрузки функций</div>';
    }
}

/**
 * Выбор математической функции (подсветка)
 */
function selectMathFunction(type, element) {
    // Снимаем подсветку со всех
    document.querySelectorAll('.math-function').forEach(el => {
        el.style.background = '';
        el.style.color = '';
    });

    // Подсвечиваем выбранную
    element.style.background = 'var(--accent-orange)';
    element.style.color = 'white';

    // Сохраняем выбор
    selectedMathFunction = type;
}

/**
 * Создание табулированной функции ПО ТОЧКАМ
 */
async function createFunctionByPoints() {
    const name = document.getElementById('pointsName').value.trim();
    const xStr = document.getElementById('pointsX').value.trim();
    const yStr = document.getElementById('pointsY').value.trim();

    // Парсинг массивов
    const xValues = xStr.split(',').map(s => parseFloat(s.trim())).filter(n => !isNaN(n));
    const yValues = yStr.split(',').map(s => parseFloat(s.trim())).filter(n => !isNaN(n));

    // ПОЛНАЯ ВАЛИДАЦИЯ
    if (!name) {
        alert('Введите название функции!');
        document.getElementById('pointsName').focus();
        return;
    }
    if (xValues.length < 2 || yValues.length < 2) {
        alert(' Минимум 2 точки для X и Y!');
        document.getElementById('pointsX').focus();
        return;
    }
    if (xValues.length !== yValues.length) {
        alert(' Количество X и Y точек должно совпадать!');
        document.getElementById('pointsY').focus();
        return;
    }

    // Проверка строгой монотонности X
    for (let i = 1; i < xValues.length; i++) {
        if (xValues[i] <= xValues[i-1]) {
            alert(' X значения должны СТРОГО возрастать!');
            document.getElementById('pointsX').focus();
            return;
        }
    }

    try {
        showLoading(true, 'points');

        const response = await fetch('/api/v1/functions/tabulated/by-points', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name,
                xValues,
                yValues
            })
        });

        if (response.ok) {
            closePointsModal();
            alert(' Табулированная функция создана успешно!');
        } else {
            const error = await response.json().catch(() => ({}));
            alert(` Ошибка: ${error.error || error.message || 'Не удалось создать функцию'}`);
        }
    } catch (error) {
        console.error('Ошибка создания функции по точкам:', error);
        alert(' Ошибка сети: ' + error.message);
    } finally {
        showLoading(false, 'points');
    }
}

/**
 * Создание табулированной функции ПО МАТЕМАТИЧЕСКОЙ ФОРМУЛЕ
 */
async function createFunctionByMath() {
    const name = document.getElementById('mathName').value.trim();
    const mathFunctionType = selectedMathFunction;
    const fromX = parseFloat(document.getElementById('mathFromX').value);
    const toX = parseFloat(document.getElementById('mathToX').value);
    const pointsCount = parseInt(document.getElementById('mathPointsCount').value);

    //  ПОЛНАЯ ВАЛИДАЦИЯ
    if (!name) {
        alert(' Введите название функции!');
        document.getElementById('mathName').focus();
        return;
    }
    if (!mathFunctionType) {
        alert(' Выберите тип функции из списка!');
        return;
    }
    if (isNaN(fromX) || isNaN(toX) || fromX >= toX) {
        alert(' fromX должно быть МЕНЬШЕ toX!');
        document.getElementById('mathFromX').focus();
        return;
    }
    if (isNaN(pointsCount) || pointsCount < 2 || pointsCount > 1000) {
        alert(' Количество точек: 2-1000!');
        document.getElementById('mathPointsCount').focus();
        return;
    }

    try {
        showLoading(true, 'math');

        const response = await fetch('/api/v1/functions/tabulated/by-math-function', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name,
                mathFunctionType,
                fromX,
                toX,
                pointsCount
            })
        });

        if (response.ok) {
            closeMathModal();
            alert(' Табулированная функция создана успешно!');
        } else {
            const error = await response.json().catch(() => ({}));
            alert(` Ошибка: ${error.error || error.message || 'Не удалось создать функцию'}`);
        }
    } catch (error) {
        console.error('Ошибка создания функции по формуле:', error);
        alert(' Ошибка сети: ' + error.message);
    } finally {
        showLoading(false, 'math');
    }
}

// ============================================================================
//  ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ

/**
 * Показать/скрыть загрузку
 */
function showLoading(show, modalType = 'points') {
    const selector = modalType === 'math' ? '#mathModal .btn-create' : '.btn-create';
    const btns = document.querySelectorAll(selector);

    btns.forEach(btn => {
        btn.disabled = show;
        btn.textContent = show ? 'Создаётся...' : 'Создать';
    });
}

/**
 * Закрытие модалок по клику вне них
 */
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        closePointsModal();
        closeMathModal();
    }
};

/**
 * Обработка клавиш ESC
 */
window.onkeydown = function(event) {
    if (event.key === 'Escape') {
        closePointsModal();
        closeMathModal();
    }
};


// АВТОЗАГРУЗКА ПРИ ОТКРЫТИИ СТРАНИЦЫ
window.onload = function() {
    loadProfile();
};