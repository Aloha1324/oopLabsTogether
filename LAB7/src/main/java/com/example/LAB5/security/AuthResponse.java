package com.example.LAB5.security;

public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private Long userId;
    private String email;

    // Конструктор для всех полей
    public AuthResponse(String token, String username, String role, Long userId, String email) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.userId = userId;
        this.email = email;
    }

    // Конструктор без email (для обратной совместимости)
    public AuthResponse(String token, String username, String role, Long userId) {
        this(token, username, role, userId, "");
    }

    // Геттеры
    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    // Сеттеры (если нужны)
    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}