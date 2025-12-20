package com.example.LAB5.ui;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

// Кастомные исключения (вложенные статические классы)
class FunctionOperationException extends RuntimeException {
    public FunctionOperationException(String message) {
        super(message);
    }
}

class FactoryException extends RuntimeException {
    public FactoryException(String message) {
        super(message);
    }
}

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Обработка ошибок валидации данных (некорректный ввод пользователя)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidation(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "validation.failed", "message", e.getMessage())
        );
    }

    /**
     * Обработка ошибок авторизации (отсутствует/недействительный JWT)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurity(SecurityException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of("error", "unauthorized", "message", "Требуется аутентификация")
        );
    }

    /**
     * Обработка ошибок при операциях над функциями
     * (несовпадение точек, деление на ноль и т.д.)
     */
    @ExceptionHandler(FunctionOperationException.class)
    public ResponseEntity<Map<String, String>> handleFunctionOperation(FunctionOperationException e) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "function.operation.failed", "message", e.getMessage())
        );
    }

    /**
     * Обработка ошибок при работе с фабрикой
     */
    @ExceptionHandler(FactoryException.class)
    public ResponseEntity<Map<String, String>> handleFactory(FactoryException e) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "factory.error", "message", e.getMessage())
        );
    }

    /**
     * Обработка всех остальных непредвиденных ошибок
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception e) {
        log.error("Непредвиденная ошибка в приложении", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "internal.error", "message", "Произошла внутренняя ошибка сервера")
        );
    }
}