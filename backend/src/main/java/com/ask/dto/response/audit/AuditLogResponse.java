package com.ask.dto.response.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for an audit log entry.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
}
