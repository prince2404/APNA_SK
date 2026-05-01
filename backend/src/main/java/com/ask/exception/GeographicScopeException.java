package com.ask.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user tries to access data outside their geographic scope.
 * Results in a 403 Forbidden — NOT an empty array.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class GeographicScopeException extends RuntimeException {
    public GeographicScopeException(String message) {
        super(message);
    }

    public GeographicScopeException() {
        super(com.ask.constants.ErrorMessages.GEOGRAPHIC_SCOPE_VIOLATION);
    }
}
