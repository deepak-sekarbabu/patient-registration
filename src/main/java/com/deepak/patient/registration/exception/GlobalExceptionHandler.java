package com.deepak.patient.registration.exception;

import com.deepak.patient.registration.repository.PatientRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for the patient registration service. Handles various types of
 * exceptions and returns appropriate HTTP responses.
 */
@RequiredArgsConstructor
@RestControllerAdvice
@org.springframework.stereotype.Component("patientGlobalExceptionHandler")
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final PatientRepository patientRepository;

  @ExceptionHandler(Exception.class)
  @SuppressWarnings("CallToPrintStackTrace")
  public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
    LOGGER.error("An error occurred: {}", ex.getMessage(), ex);
    ErrorDetails errorDetails =
        new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
    return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex, WebRequest request) {
    LOGGER.error("Data integrity violation: {}", ex.getMessage(), ex);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().format(FORMATTER));
    body.put("status", HttpStatus.CONFLICT.value());
    body.put("error", "Data Integrity Violation");

    String message = ex.getMostSpecificCause().getMessage();
    // Custom handling for duplicate key violations
    if (message != null) {
      if (message.contains("uq_phone_number")) {
        body.put("message", "Phone number already exists. Please use a different phone number.");
      } else if (message.contains("Duplicate entry")) {
        body.put("message", "Duplicate entry. The provided data already exists.");
      } else {
        body.put("message", message);
      }
    } else {
      body.put("message", "A data integrity violation occurred.");
    }

    body.put("path", request.getDescription(false).replace("uri=", ""));
    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorDetails> handleBadCredentialsException(
      BadCredentialsException ex, WebRequest request) {
    LOGGER.warn("Authentication failed: {}", ex.getMessage());

    // Extract phone number from request body (since we use phone for login)
    String phoneNumber = extractPhoneNumberFromRequest(request);

    // Update failed login attempts if we found the phone number
    if (phoneNumber != null && !phoneNumber.isBlank()) {
      patientRepository
          .findByPhoneNumber(phoneNumber)
          .ifPresent(
              patient -> {
                int attempts = patient.getFailedLoginAttempts() + 1;
                patient.setFailedLoginAttempts(attempts);

                // Lock the account after too many failed attempts
                if (attempts >= 5) { // Assuming 5 failed attempts before lockout
                  patient.setLockedUntil(OffsetDateTime.now().plusHours(1)); // Lock for 1 hour
                  LOGGER.warn("Account locked for phone number: {}", phoneNumber);
                }

                patientRepository.save(patient);
              });
    }

    ErrorDetails errorDetails =
        new ErrorDetails(
            LocalDateTime.now(), "Invalid phone number or password", request.getDescription(false));

    return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(LockedException.class)
  public ResponseEntity<ErrorDetails> handleLockedException(
      LockedException ex, WebRequest request) {
    LOGGER.warn("Account locked: {}", ex.getMessage());
    String message = "Account is locked. Please try again later or contact support.";
    ErrorDetails errorDetails =
        new ErrorDetails(LocalDateTime.now(), message, request.getDescription(false));
    return new ResponseEntity<>(errorDetails, HttpStatus.LOCKED); // 423 Locked
  }

  @ExceptionHandler({
    ValidationException.class,
    WebExchangeBindException.class,
    MethodArgumentNotValidException.class,
    ResponseStatusException.class,
    ConstraintViolationException.class
  })
  public ResponseEntity<Object> handleValidationExceptions(Exception ex, WebRequest request) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().format(FORMATTER));
    body.put("details", request.getDescription(false));
    HttpStatus status = HttpStatus.BAD_REQUEST; // Default status

    List<String> errors = new ArrayList<>();

    if (ex instanceof MethodArgumentNotValidException validationException) {
      errors =
          validationException.getBindingResult().getFieldErrors().stream()
              .map(error -> error.getField() + ": " + error.getDefaultMessage())
              .collect(Collectors.toList());
    } else if (ex instanceof WebExchangeBindException bindException) {
      errors =
          bindException.getBindingResult().getFieldErrors().stream()
              .map(error -> error.getField() + ": " + error.getDefaultMessage())
              .collect(Collectors.toList());
    } else if (ex instanceof ResponseStatusException responseStatusException) {
      if (responseStatusException.getStatusCode().value() == 404) {
        status = HttpStatus.NOT_FOUND;
        if (responseStatusException.getReason() != null) {
          errors.add(responseStatusException.getReason());
        } else {
          errors.add("Not Found");
        }
      } else {
        errors.add(responseStatusException.getReason());
      }
    } else if (ex != null) {
      errors.add(ex.getMessage());
    }

    body.put("status", status.value());
    if (!errors.isEmpty()) {
      body.put("errors", errors);
    }

    return new ResponseEntity<>(body, status);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorDetails> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    ErrorDetails errorDetails =
        new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
    LOGGER.error("Input Argument Not in Expected Format: {}", ex.getMessage());
    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<ErrorDetails> handleNotFound(Exception ex, WebRequest request) {
    LOGGER.warn("Resource not found: {}", ex.getMessage());
    ErrorDetails errorDetails =
        new ErrorDetails(
            LocalDateTime.now(),
            "Resource not found. Please check the URL or refer to the API documentation at /swagger-ui.html",
            request.getDescription(false));
    return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
  }

  /**
   * Extracts phone number from the web request.
   *
   * @param request The web request
   * @return The extracted phone number or null if not found
   */
  private String extractPhoneNumberFromRequest(WebRequest request) {
    try {
      String body = request.getParameter("phoneNumber");
      if (body != null) {
        return body;
      } else {
        // Try to parse from request description if possible
        String desc = request.getDescription(false);
        if (desc.contains("phoneNumber")) {
          int idx = desc.indexOf("phoneNumber=");
          if (idx != -1) {
            int end = desc.indexOf(',', idx);
            if (end == -1) end = desc.length();
            return desc.substring(idx + 11, end).replaceAll("[&=]", "").trim();
          }
        }
      }
    } catch (Exception ignored) {
      // Ignore if we can't extract the phone number
    }
    return null;
  }

  /** Error details model for consistent error responses. */
  public record ErrorDetails(LocalDateTime timestamp, String message, String details) {}
}
