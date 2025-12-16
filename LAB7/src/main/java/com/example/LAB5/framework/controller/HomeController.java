package com.example.LAB5.framework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HomeController {

    // ✅ ГЛАВНАЯ → JWT авторизация (index.html)
    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    // ✅ Health check (всегда доступен)
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
