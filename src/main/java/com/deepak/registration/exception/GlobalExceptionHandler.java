package com.deepak.registration.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "A data integrity violation occurred.";
        Throwable rootCause = (ex != null) ? ex.getRootCause() : null;
        if (rootCause != null && rootCause.getMessage() != null && rootCause.getMessage().contains("uq_phone_number")) {
            message = "User Already exists.";
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }
}
