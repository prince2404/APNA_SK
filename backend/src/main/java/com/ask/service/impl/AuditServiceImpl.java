package com.ask.service.impl;

import com.ask.entity.AuditLog;
import com.ask.entity.User;
import com.ask.repository.AuditLogRepository;
import com.ask.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Async implementation of audit logging service.
 * Every call runs in a background thread so it never blocks API responses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Override
    @Transactional
    public void log(User user, String action, String entityType, Long entityId,
                    String oldValue, String newValue, String ipAddress, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .description(description)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Audit logging failure should never crash the application
            log.error("Failed to save audit log: action={}, user={}, error={}",
                    action, user != null ? user.getEmail() : "null", e.getMessage());
        }
    }

    @Async
    @Override
    @Transactional
    public void log(User user, String action, String ipAddress, String description) {
        log(user, action, null, null, null, null, ipAddress, description);
    }
}
