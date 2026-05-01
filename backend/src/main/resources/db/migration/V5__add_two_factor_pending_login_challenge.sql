-- =============================================================================
-- V5__add_two_factor_pending_login_challenge.sql
-- Adds a short-lived pending-login challenge for 2FA.
-- This prevents email + OTP alone from completing login without a prior
-- successful password authentication.
-- =============================================================================

ALTER TABLE two_factor_config
    ADD COLUMN pending_login_token_hash VARCHAR(255) NULL,
    ADD COLUMN pending_login_expires_at TIMESTAMP NULL;
