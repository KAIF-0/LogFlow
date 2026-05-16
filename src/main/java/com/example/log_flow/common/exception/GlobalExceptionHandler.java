package com.example.log_flow.common.exception;

import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.common.response.ErrorInfo;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .toList();
        ErrorInfo errorInfo = new ErrorInfo("validation_failed", "Validation failed", errors);
        ApiResponse<Void> response = ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), errorInfo);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleApp(AppException exception) {
        ErrorInfo errorInfo = new ErrorInfo(exception.getCode(), exception.getMessage(), exception.getDetails());
        ApiResponse<Void> response = ApiResponse.failure(exception.getStatus().value(), errorInfo);
        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        ErrorInfo errorInfo = new ErrorInfo("invalid_request", "Invalid request parameter", List.of(exception.getName()));
        ApiResponse<Void> response = ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), errorInfo);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            String field = invalidFormat.getPath().isEmpty() ? "body" : invalidFormat.getPath().get(0).getFieldName();
            ErrorInfo errorInfo = new ErrorInfo("invalid_enum", "Invalid value for " + field, List.of(invalidFormat.getValue().toString()));
            ApiResponse<Void> response = ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), errorInfo);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        ErrorInfo errorInfo = new ErrorInfo("invalid_request", "Malformed request body", null);
        ApiResponse<Void> response = ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), errorInfo);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception exception) {
        ErrorInfo errorInfo = new ErrorInfo("internal_error", "Internal server error", null);
        ApiResponse<Void> response = ApiResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorInfo);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}