-- =============================================================================
-- V3__create_permissions_and_audit.sql
-- Creates permissions, user_permissions, permission_requests, audit_logs tables.
-- =============================================================================

-- Available permissions in the system (seeded with defaults)
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module VARCHAR(50) NOT NULL COMMENT 'e.g., USERS, PATIENTS, BILLING',
    action VARCHAR(50) NOT NULL COMMENT 'e.g., VIEW, CREATE, EDIT, DELETE',
    description VARCHAR(255),
    UNIQUE KEY uk_permissions_module_action (module, action),
    INDEX idx_permissions_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Permissions assigned to each user (checkbox-based)
CREATE TABLE user_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_by BIGINT NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id),
    CONSTRAINT fk_user_permissions_granted_by FOREIGN KEY (granted_by) REFERENCES users(id),
    UNIQUE KEY uk_user_permission (user_id, permission_id),
    INDEX idx_user_permissions_user_id (user_id),
    INDEX idx_user_permissions_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Permission requests from users to Super Admin
CREATE TABLE permission_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    reason TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_perm_requests_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_perm_requests_permission FOREIGN KEY (permission_id) REFERENCES permissions(id),
    CONSTRAINT fk_perm_requests_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id),
    INDEX idx_perm_requests_user_id (user_id),
    INDEX idx_perm_requests_status (status),
    INDEX idx_perm_requests_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit log — every action by every user is logged here, never deleted
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL COMMENT 'e.g., LOGIN, CREATE_USER, UPDATE_BILL',
    entity_type VARCHAR(50) COMMENT 'e.g., USER, BILL, PATIENT',
    entity_id BIGINT COMMENT 'ID of the affected record',
    old_value JSON COMMENT 'Previous state of the record (for updates)',
    new_value JSON COMMENT 'New state of the record (for creates/updates)',
    ip_address VARCHAR(45),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_audit_logs_user_id (user_id),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_entity_type (entity_type),
    INDEX idx_audit_logs_entity_id (entity_id),
    INDEX idx_audit_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- System configuration — key-value store for runtime-configurable settings
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_system_config_updated_by FOREIGN KEY (updated_by) REFERENCES users(id),
    INDEX idx_system_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
