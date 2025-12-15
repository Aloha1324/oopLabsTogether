package com.example.LAB5.DTO.Response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private boolean success = false;
    private String message;
    private String error;
    private int status;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, String error, int status) {
        this.message = message;
        this.error = error;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, int status) {
        this(message, null, status);
    }
}