-- =============================================================================
-- V6__soft_revoke_refresh_tokens.sql
-- Refresh tokens are records too; revoke them instead of hard deleting them.
-- =============================================================================

ALTER TABLE refresh_tokens
    ADD COLUMN is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN revoked_at TIMESTAMP NULL;

CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens (is_revoked);
