package com.deepak.registration.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<String> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    String message = "A data integrity violation occurred.";
    Throwable rootCause = (ex != null) ? ex.getRootCause() : null;
    if (rootCause != null
        && rootCause.getMessage() != null
        && rootCause.getMessage().contains("uq_phone_number")) {
      message = "User Already exists.";
    }
    return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<String> handleNotFound(Exception ex) {
    String message =
        "Resource not found. Please check the URL or refer to the API documentation at /swagger-ui.html";
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
  }
}
