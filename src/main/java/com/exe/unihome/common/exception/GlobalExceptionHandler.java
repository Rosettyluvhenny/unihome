package com.exe.unihome.common.exception;

import com.exe.unihome.common.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex, WebRequest request) {
    ErrorCode errorCode = ex.getErrorCode();
    log.error("AppException: {}", errorCode.getMessage());

    return ResponseEntity
      .status(errorCode.getStatusCode())
      .body(ApiResponse.builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
    MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
      errors.put(error.getField(), error.getDefaultMessage())
    );

    log.error("Validation failed: {}", errors);

    return ResponseEntity
      .badRequest()
      .body(ApiResponse.<Map<String, String>>builder()
        .code(ErrorCode.INVALID_REQUEST.getCode())
        .message("Validation failed")
        .data(errors)
        .build());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
    IllegalArgumentException ex, WebRequest request) {
    log.error("IllegalArgumentException: {}", ex.getMessage());

    return ResponseEntity
      .badRequest()
      .body(ApiResponse.builder()
        .code(ErrorCode.INVALID_REQUEST.getCode())
        .message(ex.getMessage())
        .build());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
    AccessDeniedException ex, WebRequest request) {
    log.warn("Access denied: {}", ex.getMessage());
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ApiResponse.builder()
        .code(HttpStatus.FORBIDDEN.value())
        .message("Access denied")
        .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleGeneralException(
    Exception ex, WebRequest request) {
    log.error("Unexpected exception occurred", ex);

    return ResponseEntity
      .internalServerError()
      .body(ApiResponse.builder()
        .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
        .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
        .build());
  }
}

