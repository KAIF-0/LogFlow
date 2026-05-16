package com.example.log_flow.common.exception;

import com.example.log_flow.common.response.ApiResponse;
import com.example.log_flow.common.response.ErrorInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class NotFoundExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException exception) {
        ErrorInfo errorInfo = new ErrorInfo("not_found", "Endpoint not found", null);
        ApiResponse<Void> response = ApiResponse.failure(HttpStatus.NOT_FOUND.value(), errorInfo);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}