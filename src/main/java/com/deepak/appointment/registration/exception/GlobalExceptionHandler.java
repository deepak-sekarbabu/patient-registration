package com.deepak.appointment.registration.exception;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for the application that provides centralized exception handling across
 * all {@code @RequestMapping} methods through {@code @ExceptionHandler} methods.
 */
@ControllerAdvice(
    basePackages = {
      "com.deepak.appointment.registration.controller",
      "com.deepak.patient.registration.controller"
    })
@org.springframework.stereotype.Component("globalExceptionHandler")
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private String generateRequestId() {
    return "req_" + UUID.randomUUID().toString().substring(0, 8);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(
      ConflictException ex, WebRequest request) {
    String requestId = generateRequestId();
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();

    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            path,
            requestId);

    logger.warn(
        "Conflict detected - Request ID: {}, Path: {}, Message: {}",
        requestId,
        path,
        ex.getMessage());

    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(
      NotFoundException ex, WebRequest request) {
    String requestId = generateRequestId();
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();

    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            path,
            requestId);

    logger.info(
        "Resource not found - Request ID: {}, Path: {}, Message: {}",
        requestId,
        path,
        ex.getMessage());

    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {
    String requestId = generateRequestId();
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();

    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error -> {
                  Object rejectedValue = error.getRejectedValue();
                  String valueStr = rejectedValue == null ? "null" : rejectedValue.toString();
                  return String.format(
                      "%s: %s (rejected value: %s)",
                      error.getField(), error.getDefaultMessage(), valueStr);
                })
            .collect(Collectors.joining("; "));

    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            "Request validation failed: " + errorMessage,
            path,
            requestId);

    logger.warn(
        "Validation failed - Request ID: {}, Path: {}, Errors: {}", requestId, path, errorMessage);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, WebRequest request) {
    String requestId = generateRequestId();
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();

    String message =
        "Data integrity violation. This may be due to a duplicate entry or invalid reference.";

    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Data Integrity Error",
            message,
            path,
            requestId);

    logger.error(
        "Data integrity violation - Request ID: {}, Path: {}, Error: {}",
        requestId,
        path,
        ex.getMessage(),
        ex);

    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class,
    HttpRequestMethodNotSupportedException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequestExceptions(
      Exception ex, WebRequest request) {
    String requestId = generateRequestId();
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();

    String message = "Invalid request: " + ex.getMessage();

    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Request",
            message,
            path,
            requestId);

    logger.warn(
        "Bad request - Request ID: {}, Path: {}, Error: {}", requestId, path, ex.getMessage());

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(
      ObjectOptimisticLockingFailureException ex, WebRequest request) {
    String requestId = generateRequestId();
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();

    String message = "The data was modified by another transaction. Please refresh and try again.";

    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Concurrent Modification",
            message,
            path,
            requestId);

    logger.warn("Optimistic lock failure - Request ID: {}, Path: {}", requestId, path, ex);

    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
    String requestId = generateRequestId();
    String path = ((ServletWebRequest) request).getRequest().getRequestURI();

    String message = "An unexpected error occurred. Please try again later or contact support.";

    ErrorResponse response =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            message,
            path,
            requestId);

    logger.error(
        "Unexpected error - Request ID: {}, Path: {}, Error: {}",
        requestId,
        path,
        ex.getMessage(),
        ex);

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Standardized error response body. */
  @lombok.Data
  @lombok.Builder
  @lombok.NoArgsConstructor
  @lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
  public static class ErrorResponse {
    /** ISO-8601 timestamp of when the error occurred. */
    private LocalDateTime timestamp;

    /** HTTP status code. */
    private int status;

    /** Error type/category. */
    private String error;

    /** Human-readable error message. */
    private String message;

    /** API endpoint path where the error occurred. */
    private String path;

    /** Unique request identifier for tracing. */
    private String requestId;
  }
}
