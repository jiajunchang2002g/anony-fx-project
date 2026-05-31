package com.example.paymentplatform.exception;

import com.example.paymentplatform.dto.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class,
                     ConstraintViolationException.class})
  public ResponseEntity<ApiErrorResponse>
  handleBadRequest(Exception exception, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(),
                 List.of(), request.getRequestURI());
  }

  @ExceptionHandler({NotFoundException.class, EntityNotFoundException.class})
  public ResponseEntity<ApiErrorResponse>
  handleNotFound(Exception exception, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(),
                 List.of(), request.getRequestURI());
  }

  @ExceptionHandler({ConflictException.class,
                     DataIntegrityViolationException.class})
  public ResponseEntity<ApiErrorResponse>
  handleConflict(Exception exception, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(),
                 List.of(), request.getRequestURI());
  }

  @ExceptionHandler({InsufficientBalanceException.class})
  public ResponseEntity<ApiErrorResponse>
  handleInsufficient(InsufficientBalanceException exception,
                     HttpServletRequest request) {
    return build(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_BALANCE",
                 exception.getMessage(), List.of(), request.getRequestURI());
  }

  @ExceptionHandler({BadCredentialsException.class, UnauthorizedException.class,
                     AccessDeniedException.class})
  public ResponseEntity<ApiErrorResponse>
  handleUnauthorized(Exception exception, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                 exception.getMessage(), List.of(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse>
  handleValidation(MethodArgumentNotValidException exception,
                   HttpServletRequest request) {
    List<String> details =
        exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .toList();
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                 "Request validation failed", details, request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse>
  handleFallback(Exception exception, HttpServletRequest request) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                 exception.getMessage(), List.of(), request.getRequestURI());
  }

  private ResponseEntity<ApiErrorResponse>
  build(HttpStatus status, String errorCode, String message,
        List<String> details, String path) {
    return ResponseEntity.status(status).body(
        new ApiErrorResponse(errorCode, message, details, path, Instant.now()));
  }
}
