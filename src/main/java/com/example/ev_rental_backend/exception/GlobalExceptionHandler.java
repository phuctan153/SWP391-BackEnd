package com.example.ev_rental_backend.exception;

import com.example.ev_rental_backend.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🔹 Bắt lỗi validate (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .status("error")
                .code(HttpStatus.BAD_REQUEST.value())
                .data(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // 🔹 Bắt lỗi RuntimeException (lỗi business logic)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException e) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status("error")
                .code(HttpStatus.BAD_REQUEST.value())
                .data(e.getMessage())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // 🔹 Bắt lỗi truy cập bị từ chối (AccessDenied)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException e) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status("error")
                .code(HttpStatus.FORBIDDEN.value())
                .data("Bạn không có quyền truy cập tài nguyên này.")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 🔹 Bắt các lỗi hệ thống khác
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception e) {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .status("error")
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .data("Lỗi hệ thống: " + e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
