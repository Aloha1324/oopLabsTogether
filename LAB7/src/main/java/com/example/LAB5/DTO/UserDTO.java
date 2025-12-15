package com.example.LAB5.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String status;
    private int functionCount;
    private int pointCount;
    private int operationCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    private String firstName;
    private String lastName;
    private String avatarUrl;
    private String theme;
    private String language;
    private boolean emailNotifications;

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

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public int getFunctionCount() { return functionCount; }
    public int getPointCount() { return pointCount; }
    public int getOperationCount() { return operationCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getTheme() { return theme; }
    public String getLanguage() { return language; }
    public boolean isEmailNotifications() { return emailNotifications; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setFunctionCount(int functionCount) { this.functionCount = functionCount; }
    public void setPointCount(int pointCount) { this.pointCount = pointCount; }
    public void setOperationCount(int operationCount) { this.operationCount = operationCount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setTheme(String theme) { this.theme = theme; }
    public void setLanguage(String language) { this.language = language; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

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