package com.example.log_flow.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final int statusCode;
    private final ErrorInfo error;
    private final T data;
    private final Pagination pagination;

    private ApiResponse(boolean success, int statusCode, ErrorInfo error, T data, Pagination pagination) {
        this.success = success;
        this.statusCode = statusCode;
        this.error = error;
        this.data = data;
        this.pagination = pagination;
    }

    public static <T> ApiResponse<T> success(int statusCode, T data) {
        return new ApiResponse<>(true, statusCode, null, data, null);
    }

    public static <T> ApiResponse<T> success(int statusCode, T data, Pagination pagination) {
        return new ApiResponse<>(true, statusCode, null, data, pagination);
    }

    public static <T> ApiResponse<T> failure(int statusCode, ErrorInfo error) {
        return new ApiResponse<>(false, statusCode, error, null, null);
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

    public Pagination getPagination() {
        return pagination;
    }
}