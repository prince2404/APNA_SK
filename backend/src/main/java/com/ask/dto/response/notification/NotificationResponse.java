package com.ask.dto.response.notification;

import com.ask.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a user notification.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private String refEntityType;
    private Long refEntityId;
    private LocalDateTime createdAt;
}
