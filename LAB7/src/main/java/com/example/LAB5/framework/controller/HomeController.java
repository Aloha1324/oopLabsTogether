package com.example.LAB5.framework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> home() {
        String html = """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Function Analyzer - LAB5</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        margin: 0;
                        padding: 0;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        color: white;
                    }
                    .container {
                        text-align: center;
                        background: rgba(0, 0, 0, 0.7);
                        padding: 40px;
                        border-radius: 15px;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.3);
                        max-width: 600px;
                        animation: fadeIn 0.8s ease-out;
                    }
                    @keyframes fadeIn {
                        from { opacity: 0; transform: translateY(-20px); }
                        to { opacity: 1; transform: translateY(0); }
                    }
                    h1 {
                        font-size: 2.5em;
                        margin-bottom: 20px;
                        color: #fff;
                    }
                    .status {
                        background: #4CAF50;
                        color: white;
                        padding: 10px 20px;
                        border-radius: 25px;
                        display: inline-block;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .btn {
                        display: inline-block;
                        margin: 10px;
                        padding: 12px 30px;
                        background: #667eea;
                        color: white;
                        text-decoration: none;
                        border-radius: 25px;
                        transition: all 0.3s;
                        font-weight: bold;
                    }
                    .btn:hover {
                        background: #764ba2;
                        transform: translateY(-3px);
                        box-shadow: 0 10px 20px rgba(0,0,0,0.2);
                    }
                    .features {
                        margin-top: 30px;
                        text-align: left;
                    }
                    .feature {
                        margin: 10px 0;
                        padding: 10px;
                        background: rgba(255,255,255,0.1);
                        border-radius: 8px;
                    }
                    .api-list {
                        margin-top: 20px;
                        text-align: left;
                        background: rgba(255,255,255,0.1);
                        padding: 15px;
                        border-radius: 8px;
                    }
                    .api-item {
                        margin: 5px 0;
                        font-family: monospace;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🚀 Function Analyzer - LAB5</h1>
                    <div class="status">✅ Сервер запущен на Tomcat!</div>
                    <p>Лабораторная работа №7 - Веб-интерфейс для анализа функций</p>
                    
                    <div>
                        <a href="/ui/index.html" class="btn">📊 Перейти к интерфейсу</a>
                        <a href="/api/auth/login" class="btn">🔐 API Авторизация</a>
                    </div>
                    
                    <div class="features">
                        <div class="feature">✅ JWT Аутентификация</div>
                        <div class="feature">✅ Создание функций по точкам</div>
                        <div class="feature">✅ Создание из математических функций</div>
                        <div class="feature">✅ Интерполяция и дифференцирование</div>
                        <div class="feature">✅ Работает на Tomcat</div>
                        <div class="feature">✅ PostgreSQL база данных</div>
                    </div>
                    
                    <div class="api-list">
                        <h4>📚 Доступные API:</h4>
                        <div class="api-item">POST /api/auth/login</div>
                        <div class="api-item">POST /api/auth/register</div>
                        <div class="api-item">GET /api/v1/functions</div>
                        <div class="api-item">POST /api/v1/functions</div>
                        <div class="api-item">GET /api/v1/points</div>
                        <div class="api-item">POST /api/v1/points</div>
                    </div>
                    
                    <div style="margin-top: 30px; font-size: 0.9em; opacity: 0.8;">
                        <p>Для начала работы перейдите по ссылке "Перейти к интерфейсу"</p>
                        <p>Используйте: admin / admin для тестового входа</p>
                    </div>
                </div>
            </body>
            </html>
            """;
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}