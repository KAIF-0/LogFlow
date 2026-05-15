package com.example.log_flow.common.response;

public class ApiResponse<T> {

    private final boolean success;
    private final int statusCode;
    private final ErrorInfo error;
    private final T data;

    private ApiResponse(boolean success, int statusCode, ErrorInfo error, T data) {
        this.success = success;
        this.statusCode = statusCode;
        this.error = error;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(int statusCode, T data) {
        return new ApiResponse<>(true, statusCode, null, data);
    }

    public static <T> ApiResponse<T> failure(int statusCode, ErrorInfo error) {
        return new ApiResponse<>(false, statusCode, error, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ErrorInfo getError() {
        return error;
    }

    public T getData() {
        return data;
    }
}