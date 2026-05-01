package com.ask.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper used by every endpoint in the application.
 * Ensures a consistent response structure for both success and error cases.
 *
 * @param <T> the type of the data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Whether the request was successful */
    private boolean success;

    /** Human-readable message describing the result */
    private String message;

    /** The response payload — can be an object, list, or null */
    private T data;

    /** Machine-readable error code (only present on errors) */
    private String errorCode;

    /** ISO 8601 timestamp of the response */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /** The API path that was called */
    private String path;

    /**
     * Creates a successful response with data and message.
     *
     * @param data    the response payload
     * @param message description of what happened
     * @param path    the API endpoint path
     * @param <T>     type of the data
     * @return a success ApiResponse
     */
    public static <T> ApiResponse<T> success(T data, String message, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .path(path)
                .build();
    }

    /**
     * Creates a successful response with data only.
     *
     * @param data the response payload
     * @param path the API endpoint path
     * @param <T>  type of the data
     * @return a success ApiResponse
     */
    public static <T> ApiResponse<T> success(T data, String path) {
        return success(data, "Request successful", path);
    }

    /**
     * Creates an error response with an error code and message.
     *
     * @param message   description of what went wrong
     * @param errorCode machine-readable error code
     * @param path      the API endpoint path
     * @param <T>       type of the data (will be null)
     * @return an error ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .build();
    }
}
