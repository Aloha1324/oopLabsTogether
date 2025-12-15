// ============================================================================
// ✅ app.js — ПОЛНАЯ РЕАЛИЗАЦИЯ ДЛЯ ЛИЧНОГО КАБИНЕТА + ТАБУЛИРОВАННЫЕ ФУНКЦИИ
// ============================================================================

let currentUser = null;
let selectedMathFunction = null;

// ============================================================================
// ✅ ОСНОВНЫЕ ФУНКЦИИ ПРОФИЛЯ
// ============================================================================

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
// ✅ ГЛОБАЛЬНЫЕ ФУНКЦИИ ДЛЯ МОДАЛОК (вызываются из HTML onclick)
// ============================================================================

window.openPointsModal = function() {
    document.getElementById('pointsModal').style.display = 'block';
    document.getElementById('pointsName').focus();
};

window.closePointsModal = function() {
    document.getElementById('pointsModal').style.display = 'none';
    document.getElementById('pointsName').value = '';
    document.getElementById('pointsX').value = '';
    document.getElementById('pointsY').value = '';
};

window.openMathModal = function() {
    document.getElementById('mathModal').style.display = 'block';
    loadMathFunctions();
    document.getElementById('mathName').focus();
};

window.closeMathModal = function() {
    document.getElementById('mathModal').style.display = 'none';
    document.getElementById('mathName').value = '';
    selectedMathFunction = null;
    document.querySelectorAll('.math-function').forEach(el => {
        el.style.background = '';
        el.style.color = '';
    });
};

window.showCreateModal = function() {
    if (confirm('Что создать?\n\n• OK = По точкам\n• Cancel = По формуле')) {
        openPointsModal();
    } else {
        openMathModal();
    }
};

// ============================================================================
// ✅ ЛОГИКА МАТЕМАТИЧЕСКИХ ФУНКЦИЙ
// ============================================================================

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
        console.error('Ошибка загрузки функций:', error);
        document.getElementById('mathFunctionsList').innerHTML =
            '<div style="color: var(--error-red);">Ошибка загрузки функций</div>';
    }
}

function selectMathFunction(type, element) {
    document.querySelectorAll('.math-function').forEach(el => {
        el.style.background = '';
        el.style.color = '';
    });
    element.style.background = 'var(--accent-orange)';
    element.style.color = 'white';
    selectedMathFunction = type;
}

// ============================================================================
// ✅ СОЗДАНИЕ ТАБУЛИРОВАННЫХ ФУНКЦИЙ
// ============================================================================

async function createFunctionByPoints() {
    const name = document.getElementById('pointsName').value.trim();
    const xStr = document.getElementById('pointsX').value.trim();
    const yStr = document.getElementById('pointsY').value.trim();

    const xValues = xStr.split(',').map(s => parseFloat(s.trim())).filter(n => !isNaN(n));
    const yValues = yStr.split(',').map(s => parseFloat(s.trim())).filter(n => !isNaN(n));

    if (!name) {
        alert('❌ Введите название функции!');
        document.getElementById('pointsName').focus();
        return;
    }
    if (xValues.length < 2 || yValues.length < 2) {
        alert('❌ Минимум 2 точки для X и Y!');
        document.getElementById('pointsX').focus();
        return;
    }
    if (xValues.length !== yValues.length) {
        alert('❌ Количество X и Y точек должно совпадать!');
        document.getElementById('pointsY').focus();
        return;
    }

    for (let i = 1; i < xValues.length; i++) {
        if (xValues[i] <= xValues[i-1]) {
            alert('❌ X значения должны СТРОГО возрастать!');
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
            body: JSON.stringify({ name, xValues, yValues })
        });

        if (response.ok) {
            closePointsModal();
            alert('✅ Функция создана успешно!');
        } else {
            const error = await response.json().catch(() => ({}));
            alert(`❌ Ошибка: ${error.error || error.message || 'Не удалось создать'}`);
        }
    } catch (error) {
        console.error('Ошибка:', error);
        alert('❌ Ошибка сети: ' + error.message);
    } finally {
        showLoading(false, 'points');
    }
}

async function createFunctionByMath() {
    const name = document.getElementById('mathName').value.trim();
    const mathFunctionType = selectedMathFunction;
    const fromX = parseFloat(document.getElementById('mathFromX').value);
    const toX = parseFloat(document.getElementById('mathToX').value);
    const pointsCount = parseInt(document.getElementById('mathPointsCount').value);

    if (!name) {
        alert('❌ Введите название функции!');
        return;
    }
    if (!mathFunctionType) {
        alert('❌ Выберите тип функции!');
        return;
    }
    if (isNaN(fromX) || isNaN(toX) || fromX >= toX) {
        alert('❌ fromX < toX!');
        return;
    }
    if (isNaN(pointsCount) || pointsCount < 2 || pointsCount > 1000) {
        alert('❌ Точки: 2-1000!');
        return;
    }

    try {
        showLoading(true, 'math');
        const response = await fetch('/api/v1/functions/tabulated/by-math-function', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, mathFunctionType, fromX, toX, pointsCount })
        });

        if (response.ok) {
            closeMathModal();
            alert('✅ Функция создана успешно!');
        } else {
            const error = await response.json().catch(() => ({}));
            alert(`❌ Ошибка: ${error.error || error.message || 'Не удалось создать'}`);
        }
    } catch (error) {
        console.error('Ошибка:', error);
        alert('❌ Ошибка сети: ' + error.message);
    } finally {
        showLoading(false, 'math');
    }
}

// ============================================================================
// ✅ ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
// ============================================================================

function showLoading(show, modalType = 'points') {
    const selector = modalType === 'math' ? '#mathModal .btn-create' : '.btn-create';
    const btns = document.querySelectorAll(selector);
    btns.forEach(btn => {
        btn.disabled = show;
        btn.textContent = show ? 'Создаётся...' : 'Создать';
    });
}

window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        closePointsModal();
        closeMathModal();
    }
};

window.onkeydown = function(event) {
    if (event.key === 'Escape') {
        closePointsModal();
        closeMathModal();
    }
};

window.onload = function() {
    loadProfile();
};
