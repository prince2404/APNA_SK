package com.ask.service;

import com.ask.entity.User;

/**
 * Audit logging service. All methods are @Async so they never slow down API responses.
 */
public interface AuditService {

    /**
     * Logs an action performed by a user.
     *
     * @param user        the user who performed the action
     * @param action      the action performed (e.g., LOGIN, CREATE_USER)
     * @param entityType  the type of entity affected (e.g., USER, BILL)
     * @param entityId    the ID of the affected entity
     * @param oldValue    previous state as JSON (for updates)
     * @param newValue    new state as JSON (for creates/updates)
     * @param ipAddress   the client IP address
     * @param description human-readable description
     */
    void log(User user, String action, String entityType, Long entityId,
             String oldValue, String newValue, String ipAddress, String description);

    /**
     * Simplified log method for actions without entity changes.
     *
     * @param user        the user who performed the action
     * @param action      the action performed
     * @param ipAddress   the client IP address
     * @param description human-readable description
     */
    void log(User user, String action, String ipAddress, String description);
}
