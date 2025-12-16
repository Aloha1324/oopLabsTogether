package com.example.LAB5.framework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    // ✅ ГЛАВНАЯ → JWT авторизация (index.html)
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "redirect:/index.html";
    }

    // ✅ SPA роутинг — все пути ведут к index.html
    @GetMapping(value = "/{path:[^\\.]*}", produces = MediaType.TEXT_HTML_VALUE)
    public String spaRoutes() {
        return "redirect:/index.html";
    }

    // ✅ Health check (всегда доступен)
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
