package com.example.LAB5.framework.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        String html = """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="UTF-8">
                <title>LAB5 Manual API</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }
                    h1 { color: #2c3e50; }
                    h2 { color: #34495e; margin-top: 30px; }
                    .endpoint { 
                        background: #f8f9fa; 
                        padding: 10px; 
                        margin: 8px 0; 
                        border-left: 4px solid #3498db; 
                        font-family: monospace; 
                    }
                </style>
            </head>
            <body>
                <h1>LAB5 Framework API — Tomcat запущен</h1>
                 <div class="status-message">
                                     Проект работает на Tomcat! Реализовано через framework
                 </div>
                <p>Ниже представлен краткий обзор основных конечных точек HTTP.</p>

                <h2>Конечные точки для пользователей</h2>
                <div class="endpoint">GET /users — список всех пользователей</div>
                <div class="endpoint">GET /users/{id} — получение пользователя по ID</div>
                <div class="endpoint">GET /users/login/{login} — получение по логину</div>
                <div class="endpoint">GET /users/role/{role} — список по роли</div>
                <div class="endpoint">POST /users — создание пользователя</div>
                <div class="endpoint">PUT /users/{id} — обновление</div>
                <div class="endpoint">DELETE /users/{id} — удаление</div>

                <h2>Конечные точки функций</h2>
                <div class="endpoint">GET /functions — все функции</div>
                <div class="endpoint">GET /functions/{id} — функция по ID</div>
                <div class="endpoint">GET /functions/user/{userId} — функции пользователя</div>
                <div class="endpoint">GET /functions/name/{name} — поиск по имени</div>
                <div class="endpoint">GET /functions/stats/{functionId} — статистика</div>
                <div class="endpoint">POST /functions — создание</div>
                <div class="endpoint">PUT /functions/{id} — обновление</div>
                <div class="endpoint">DELETE /functions/{id} — удаление</div>

                <h2>Конечные точки точек</h2>
                <div class="endpoint">GET /points — вход в API баллов</div>
            </body>
            </html>
            """;
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}