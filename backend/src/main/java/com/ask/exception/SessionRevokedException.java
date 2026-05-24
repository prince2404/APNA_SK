package com.ask.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when a user's session has been revoked (e.g., due to a login from another device).
 */
public class SessionRevokedException extends AuthenticationException {
    public SessionRevokedException(String message) {
        super(message);
    }
}
