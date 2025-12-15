package com.example.LAB5.DTO.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class FunctionResponse {
    private Long id;
    private String name;
    private String expression;
    private LocalDateTime createdAt;
    private Long userId;
    private String username;
    private List<com.example.LAB5.DTO.PointDTO> points;
    private boolean success;
    private String message;

    public FunctionResponse() {}

    public FunctionResponse(Long id, String name, String expression,
                            LocalDateTime createdAt, Long userId, String username) {
        this.id = id;
        this.name = name;
        this.expression = expression;
        this.createdAt = createdAt;
        this.userId = userId;
        this.username = username;
        this.success = true;
        this.message = "Success";
    }

    public static FunctionResponse success(Long id, String name, String expression,
                                           LocalDateTime createdAt, Long userId, String username) {
        return new FunctionResponse(id, name, expression, createdAt, userId, username);
    }

    public static FunctionResponse error(String message) {
        FunctionResponse response = new FunctionResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<com.example.LAB5.DTO.PointDTO> getPoints() {
        return points;
    }

    public void setPoints(List<com.example.LAB5.DTO.PointDTO> points) {
        this.points = points;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionResponse that = (FunctionResponse) o;
        return success == that.success &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(expression, that.expression) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(username, that.username) &&
                Objects.equals(points, that.points) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, expression, createdAt, userId, username, points, success, message);
    }

    @Override
    public String toString() {
        return "FunctionResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", expression='" + expression + '\'' +
                ", createdAt=" + createdAt +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", points=" + points +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}