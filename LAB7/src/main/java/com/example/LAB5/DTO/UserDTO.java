package com.example.LAB5.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * DTO для передачи информации о пользователе
 * Безопасный - не содержит пароль и другую чувствительную информацию
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String role; // "USER", "ADMIN", "MODERATOR"
    private String status; // "ACTIVE", "INACTIVE", "BANNED"

    // Статистика пользователя
    private int functionCount;
    private int pointCount;
    private int operationCount;

    // Временные метки
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    // Профиль пользователя (опционально)
    private String firstName;
    private String lastName;
    private String avatarUrl;

    // Настройки пользователя (для UI)
    private String theme; // "LIGHT", "DARK", "AUTO"
    private String language; // "RU", "EN"
    private boolean emailNotifications;

    // Конструкторы
    public UserDTO() {}

    public UserDTO(Long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = "ACTIVE";
        this.theme = "DARK";
        this.language = "RU";
    }

    public UserDTO(Long id, String username, String email, String role,
                   int functionCount, int pointCount, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.functionCount = functionCount;
        this.pointCount = pointCount;
        this.createdAt = createdAt;
        this.status = "ACTIVE";
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFunctionCount() { return functionCount; }
    public void setFunctionCount(int functionCount) { this.functionCount = functionCount; }

    public int getPointCount() { return pointCount; }
    public void setPointCount(int pointCount) { this.pointCount = pointCount; }

    public int getOperationCount() { return operationCount; }
    public void setOperationCount(int operationCount) { this.operationCount = operationCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    // Вспомогательные методы
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return username;
    }

    public boolean hasAvatar() {
        return avatarUrl != null && !avatarUrl.isEmpty();
    }

    public int getTotalDataPoints() {
        return functionCount + pointCount;
    }

    @Override
    public String toString() {
        return String.format("UserDTO{id=%d, username='%s', role='%s', functions=%d}",
                id, username, role, functionCount);
    }
}