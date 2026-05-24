-- =============================================================================
-- V10__create_messaging_and_templates.sql
-- Creates tables for template management and bulk messaging logs.
-- =============================================================================

CREATE TABLE message_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    channel VARCHAR(20) NOT NULL COMMENT 'e.g., EMAIL, SMS',
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bulk_message_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL COMMENT 'e.g., EMAIL, SMS',
    target_criteria VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sent_count INT NOT NULL,
    status VARCHAR(20) NOT NULL COMMENT 'e.g., PENDING, SUCCESS, FAILED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bulk_message_logs_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    INDEX idx_bulk_msg_sender (sender_id),
    INDEX idx_bulk_msg_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
