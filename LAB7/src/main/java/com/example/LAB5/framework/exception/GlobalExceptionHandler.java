// com.example.LAB5.framework.exception.GlobalExceptionHandler.java
package com.example.LAB5.framework.exception;

import com.example.LAB5.exceptions.InconsistentFunctionsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity.internalServerError().body(Map.of("error", "Внутренняя ошибка сервера"));
    }

    @ExceptionHandler(InconsistentFunctionsException.class)
    public ResponseEntity<Map<String, String>> handleInconsistentFunctions(InconsistentFunctionsException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Несовместимые функции");
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}