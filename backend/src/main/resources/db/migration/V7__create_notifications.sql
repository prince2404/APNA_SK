-- =============================================================================
-- V7__create_notifications.sql
-- Creates the notifications table for in-app notifications.
-- =============================================================================

CREATE TABLE notifications (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    type         VARCHAR(50) NOT NULL COMMENT 'NotificationType enum value',
    title        VARCHAR(255) NOT NULL,
    message      TEXT,
    is_read      BOOLEAN     NOT NULL DEFAULT FALSE,
    ref_entity_type VARCHAR(50)  NULL COMMENT 'e.g., PERMISSION_REQUEST',
    ref_entity_id   BIGINT       NULL COMMENT 'ID of the related entity',
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_notifications_user_id  (user_id),
    INDEX idx_notifications_is_read  (user_id, is_read),
    INDEX idx_notifications_created  (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
