package br.com.gustavo.shared.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        StandardError error = new StandardError(
                LocalDateTime.now(),
                422,
                "Business Rule Violation",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(422).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ValidationError error = new ValidationError(
                LocalDateTime.now(),
                400,
                "Validation Error",
                "One or more fields are invalid",
                request.getRequestURI()
        );

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            error.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(400).body(error);
    }

    public record StandardError(
            LocalDateTime timestamp,
            Integer status,
            String error,
            String message,
            String path
    ) {}

    public static class ValidationError {
        private final LocalDateTime timestamp;
        private final Integer status;
        private final String error;
        private final String message;
        private final String path;
        private final Map<String, String> fieldErrors = new HashMap<>();

        public ValidationError(LocalDateTime timestamp, Integer status, String error, String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        public void addError(String fieldName, String errorMessage) {
            this.fieldErrors.put(fieldName, errorMessage);
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public Integer getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public Map<String, String> getFieldErrors() { return fieldErrors; }
    }
}