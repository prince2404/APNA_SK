-- =============================================================================
-- V11__add_new_permissions.sql
-- Cleans up existing permissions, permission requests, and seeds the updated
-- matrix of permissions for Geography, Stores, Users, Patients, Inventory,
-- Billing, Reports, Commissions, Notifications, and Messaging.
-- =============================================================================

-- Clear references to avoid foreign key constraint violations
DELETE FROM user_permissions;
DELETE FROM permission_requests;
DELETE FROM permissions;

-- Seed the full list of revised permissions
INSERT INTO permissions (module, action, description) VALUES
    -- GEOGRAPHY
    ('GEOGRAPHY', 'VIEW_STATES', 'View States'),
    ('GEOGRAPHY', 'VIEW_DISTRICTS', 'View Districts'),
    ('GEOGRAPHY', 'VIEW_BLOCKS', 'View Blocks'),
    ('GEOGRAPHY', 'CREATE_DISTRICT', 'Create District'),
    ('GEOGRAPHY', 'CREATE_BLOCK', 'Create Block'),

    -- STORES
    ('STORES', 'VIEW_STORES', 'View Stores'),
    ('STORES', 'CREATE_STORE', 'Create Store'),
    ('STORES', 'EDIT_STORE', 'Edit Store'),
    ('STORES', 'DEACTIVATE_STORE', 'Deactivate Store'),
    ('STORES', 'ASSIGN_STAFF', 'Assign Staff'),

    -- USERS
    ('USERS', 'VIEW_USERS', 'View Users'),
    ('USERS', 'CREATE_USER', 'Create User'),
    ('USERS', 'EDIT_USER', 'Edit User'),
    ('USERS', 'DEACTIVATE_USER', 'Deactivate User'),
    ('USERS', 'REACTIVATE_USER', 'Reactivate User'),
    ('USERS', 'VIEW_ACTIVITY_LOG', 'View Activity Log'),

    -- PATIENTS
    ('PATIENTS', 'VIEW_PATIENTS', 'View Patients'),
    ('PATIENTS', 'CREATE_PATIENT', 'Create Patient'),
    ('PATIENTS', 'EDIT_PATIENT', 'Edit Patient'),
    ('PATIENTS', 'BULK_UPLOAD', 'Bulk Upload'),

    -- INVENTORY
    ('INVENTORY', 'VIEW_STOCK', 'View Stock'),
    ('INVENTORY', 'ADD_STOCK', 'Add Stock'),
    ('INVENTORY', 'TRANSFER_STOCK', 'Transfer Stock'),
    ('INVENTORY', 'ADJUST_STOCK', 'Adjust Stock'),

    -- BILLING
    ('BILLING', 'VIEW_BILLS', 'View Bills'),
    ('BILLING', 'CREATE_BILL', 'Create Bill'),
    ('BILLING', 'CANCEL_BILL', 'Cancel Bill'),

    -- REPORTS
    ('REPORTS', 'VIEW_REPORTS', 'View Reports'),
    ('REPORTS', 'DOWNLOAD_REPORTS', 'Download Reports'),

    -- COMMISSIONS
    ('COMMISSIONS', 'VIEW_COMMISSIONS', 'View Commissions'),
    ('COMMISSIONS', 'EDIT_COMMISSION_PERCENTAGE', 'Edit Commission Percentage'),

    -- NOTIFICATIONS
    ('NOTIFICATIONS', 'VIEW_NOTIFICATIONS', 'View Notifications'),
    ('NOTIFICATIONS', 'SEND_NOTIFICATIONS', 'Send Notifications'),

    -- MESSAGING
    ('MESSAGING', 'SEND_SMS', 'Send SMS'),
    ('MESSAGING', 'SEND_EMAIL', 'Send Email'),
    ('MESSAGING', 'SEND_WHATSAPP', 'Send WhatsApp');
