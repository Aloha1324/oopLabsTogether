package com.example.LAB5.DTO.Response;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse {
    private boolean success = false;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

    public ValidationErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ValidationErrorResponse(String message, Map<String, String> errors) {
        this.message = message;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Map<String, String> getErrors() { return errors; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setErrors(Map<String, String> errors) { this.errors = errors; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}