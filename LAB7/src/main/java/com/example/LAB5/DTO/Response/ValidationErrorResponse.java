package com.example.LAB5.DTO.Response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
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
}