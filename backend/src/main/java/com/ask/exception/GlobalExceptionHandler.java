package com.ask.exception;

import com.ask.constants.ErrorMessages;
import com.ask.dto.response.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the entire application.
 * Catches every exception type and returns a consistent ApiResponse.
 * No raw stack trace ever reaches the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- Custom Application Exceptions ---

    /**
     * Handles resource not found (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "RESOURCE_NOT_FOUND", request);
    }

    /**
     * Handles duplicate resource conflict (409).
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), "DUPLICATE_RESOURCE", request);
    }

    /**
     * Handles geographic scope violation (403).
     */
    @ExceptionHandler(GeographicScopeException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeographicScope(
            GeographicScopeException ex, HttpServletRequest request) {
        log.warn("Geographic scope violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), "GEOGRAPHIC_SCOPE_VIOLATION", request);
    }

    /**
     * Handles account locked (423).
     */
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountLocked(
            AccountLockedException ex, HttpServletRequest request) {
        log.warn("Account locked: {}", ex.getMessage());
        return buildResponse(HttpStatus.LOCKED, ex.getMessage(), "ACCOUNT_LOCKED", request);
    }

    /**
     * Handles invalid request data (400).
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(
            InvalidRequestException ex, HttpServletRequest request) {
        log.warn("Invalid request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_REQUEST", request);
    }

    /**
     * Handles business rule violations (422).
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(
            BusinessRuleException ex, HttpServletRequest request) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "BUSINESS_RULE_VIOLATION", request);
    }

    // --- Spring Validation Exceptions ---

    /**
     * Handles @Valid annotation validation failures.
     * Returns field-level error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed. Please check the errors and try again")
                .errorCode("VALIDATION_ERROR")
                .data(errors)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing required request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing parameter: {}", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST,
                String.format("Required parameter '%s' is missing", ex.getParameterName()),
                "MISSING_PARAMETER", request);
    }

    /**
     * Handles type mismatch in request parameters.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch for parameter: {}", ex.getName());
        return buildResponse(HttpStatus.BAD_REQUEST,
                String.format("Parameter '%s' has an invalid value", ex.getName()),
                "TYPE_MISMATCH", request);
    }

    // --- Spring Security Exceptions ---

    /**
     * Handles bad credentials (wrong email or password).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials attempt from: {}", request.getRemoteAddr());
        return buildResponse(HttpStatus.UNAUTHORIZED, ErrorMessages.INVALID_CREDENTIALS,
                "INVALID_CREDENTIALS", request);
    }

    /**
     * Handles session revoked exception (401).
     */
    @ExceptionHandler(SessionRevokedException.class)
    public ResponseEntity<ApiResponse<Void>> handleSessionRevoked(
            SessionRevokedException ex, HttpServletRequest request) {
        log.warn("Session revoked exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "SESSION_REVOKED", request);
    }

    /**
     * Handles general authentication failures.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "AUTHENTICATION_FAILED", request);
    }

    /**
     * Handles access denied (Spring Security @PreAuthorize failures).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied for: {} to {}", request.getRemoteUser(), request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN, ErrorMessages.ACCESS_DENIED, "ACCESS_DENIED", request);
    }

    /**
     * Handles unsupported HTTP methods.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ErrorMessages.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED", request);
    }

    // --- Catch-all ---

    /**
     * Catches all unhandled exceptions. Logs full stack trace server-side
     * but returns a generic message to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: ", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR", request);
    }

    // --- Helper ---

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus status, String message, String errorCode, HttpServletRequest request) {
        ApiResponse<Void> response = ApiResponse.error(message, errorCode, request.getRequestURI());
        return new ResponseEntity<>(response, status);
    }
}
