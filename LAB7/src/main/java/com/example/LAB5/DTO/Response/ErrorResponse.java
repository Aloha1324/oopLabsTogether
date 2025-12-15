package com.example.LAB5.DTO.Response;

import java.time.LocalDateTime;

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

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getError() { return error; }
    public int getStatus() { return status; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setError(String error) { this.error = error; }
    public void setStatus(int status) { this.status = status; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}