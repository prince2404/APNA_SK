-- =============================================================================
-- V2__create_roles_and_users.sql
-- Creates roles, users, refresh_tokens, user_sessions, two_factor_config tables.
-- Users are the core entity — everything ties back to a user.
-- =============================================================================

-- Roles table — 8 fixed roles with hierarchy levels
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    hierarchy_level INT NOT NULL COMMENT 'Lower number = higher authority. 1 = Super Admin'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Users table — every person in the system
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(15) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    profile_photo_url VARCHAR(500),
    date_of_birth DATE,
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    address TEXT,
    role_id BIGINT NOT NULL,
    state_id BIGINT,
    district_id BIGINT,
    block_id BIGINT,
    store_id BIGINT,
    bank_account_encrypted TEXT COMMENT 'AES-256 encrypted bank account number',
    bank_ifsc VARCHAR(20),
    bank_name VARCHAR(100),
    pan_number VARCHAR(10),
    aadhaar_last_four VARCHAR(4) COMMENT 'Only last 4 digits stored, never full Aadhaar',
    aadhaar_doc_url VARCHAR(500) COMMENT 'Photo upload of Aadhaar card for manual review',
    verification_status ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    force_password_change BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'True when user has a temporary password',
    password_changed_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_users_state FOREIGN KEY (state_id) REFERENCES states(id),
    CONSTRAINT fk_users_district FOREIGN KEY (district_id) REFERENCES districts(id),
    CONSTRAINT fk_users_block FOREIGN KEY (block_id) REFERENCES blocks(id),
    CONSTRAINT fk_users_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_users_email (email),
    INDEX idx_users_phone (phone),
    INDEX idx_users_role_id (role_id),
    INDEX idx_users_state_id (state_id),
    INDEX idx_users_district_id (district_id),
    INDEX idx_users_block_id (block_id),
    INDEX idx_users_store_id (store_id),
    INDEX idx_users_status (status),
    INDEX idx_users_verification_status (verification_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Refresh tokens for JWT refresh flow
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_tokens_user_id (user_id),
    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User sessions — supports multiple active sessions with view/revoke
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_fingerprint VARCHAR(255) NOT NULL UNIQUE COMMENT 'SHA-256 hash of the refresh token',
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    last_active_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_sessions_user_id (user_id),
    INDEX idx_user_sessions_fingerprint (token_fingerprint),
    INDEX idx_user_sessions_revoked (is_revoked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Two-factor authentication configuration per user
CREATE TABLE two_factor_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    is_mandatory BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'True for SUPER_ADMIN and SYSTEM_ADMIN',
    otp_code VARCHAR(255) COMMENT 'Hashed OTP code',
    otp_expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_two_factor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_two_factor_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
