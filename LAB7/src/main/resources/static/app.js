const API_BASE = 'http://localhost:8080/lab7-api';

let currentToken = null;
let currentUser = null;
let factoryType = 'array';
let activeFuncA = null;
let activeFuncB = null;
let activeDiffFunc = null;
let currentChart = null;

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

function setLoading(loading) {
    document.getElementById('loading').style.display = loading ? 'block' : 'none';
}

// ===== AUTH =====
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
            wordleGame.updateFabVisibility();
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
            wordleGame.updateFabVisibility();
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

// ===== CREATE BY POINTS =====
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
        const xInput = document.getElementById(`x_${i}`).value;
        const yInput = document.getElementById(`y_${i}`).value;
        if (xInput === '' || yInput === '') return showMessage(`Ошибка в строке ${i + 1}: введите числа`);
        const x = parseFloat(xInput);
        const y = parseFloat(yInput);
        if (isNaN(x) || isNaN(y)) return showMessage(`Ошибка в строке ${i + 1}: введите корректные числа`);
        xVals.push(parseFloat(x.toFixed(10)));
        yVals.push(parseFloat(y.toFixed(10)));
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

// ===== CREATE BY FORMULA =====
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
        showMessage('Не удалось загрузить настройки: ' + err.message);
    } finally {
        setLoading(false);
    }
}

async function saveFactorySettings() {
    const selected = document.querySelector('input[name="factory"]:checked')?.value;
    if (!selected) return showMessage('Выберите тип фабрики');
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
            showMessage(err.message || 'Ошибка сохранения');
        }
    } catch (err) {
        showMessage('Ошибка сети: ' + err.message);
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
    if (!activeFuncA || !activeFuncB) return showMessage('Загрузите обе функции!');
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
                functionBId: activeFuncB.id,
                factoryType: factoryType
            })
        });
        const data = await res.json();
        if (res.ok) {
            renderFunctionTable(data, 'resultTable');
            showMessage('Операция выполнена!', 'success');
        } else {
            showMessage(data.message || 'Ошибка операции');
        }
    } catch (err) {
        showMessage('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// ===== DIFFERENTIATION =====
async function performDifferentiation() {
    if (!activeDiffFunc) return showMessage('Загрузите функцию для дифференцирования!');
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
            renderFunctionTable(data, 'diffResultTable');
            showMessage('Дифференцирование выполнено!', 'success');
        } else {
            showMessage(data.message || 'Ошибка дифференцирования');
        }
    } catch (err) {
        showMessage('Ошибка: ' + err.message);
    } finally {
        setLoading(false);
    }
}

// ===== UTILITIES =====
function renderFunctionTable(func, containerId) {
    const container = document.getElementById(containerId);
    if (!func || !func.xValues || !func.yValues) {
        container.innerHTML = '<div>Нет данных</div>';
        return;
    }
    let html = `<table><thead><tr><th>x</th><th>y</th></tr></thead><tbody>`;
    for (let i = 0; i < func.xValues.length; i++) {
        html += `<tr><td>${Number(func.xValues[i]).toFixed(4)}</td><td>${Number(func.yValues[i]).toFixed(4)}</td></tr>`;
    }
    html += `</tbody></table>`;
    container.innerHTML = html;
}

function saveResult() {
    showMessage('Сохранение результата — в разработке', 'success');
}

function saveDiffResult() {
    showMessage('Сохранение производной — в разработке', 'success');
}

function loadFunction(target) {
    showMessage(`Загрузка функции — в разработке (${target})`, 'success');
}

// ============== ГРАФИК ФУНКЦИИ ==============
async function loadFunctionsForViewer() {
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/functions`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Не удалось загрузить функции');
        }
        const functions = await res.json();
        const select = document.getElementById('functionSelect');
        select.innerHTML = '<option value="">-- Выберите функцию --</option>';
        functions.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.id;
            opt.textContent = `${f.name} (${f.type})`;
            select.appendChild(opt);
        });
    } catch (err) {
        showMessage(err.message);
    } finally {
        setLoading(false);
    }
}

async function loadFunctionForGraph() {
    const id = document.getElementById('functionSelect').value;
    if (!id) {
        clearGraphAndTable();
        return;
    }
    setLoading(true);
    try {
        const res = await fetch(`${API_BASE}/api/functions/${id}`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || 'Функция не найдена');
        }
        const func = await res.json();
        renderFunctionGraph(func);
        renderFunctionTableForGraph(func, 'functionPointsTable');
    } catch (err) {
        showMessage(err.message);
        clearGraphAndTable();
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
    if (!id) return showMessage('Сначала выберите функцию');
    const x = parseFloat(xInput);
    if (isNaN(x)) return showMessage('Введите корректное число в поле x');

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
        showMessage(err.message);
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
            showMessage('⚠️ Сначала авторизуйтесь!', 'error');
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
                showMessage(err.message || 'Ошибка создания игры');
            }
        } catch (err) {
            showMessage('Ошибка сети: ' + err.message);
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
                    this.showMessage(`Игра окончена! Слово: ${this.gameState.targetWord}`, 'error');
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