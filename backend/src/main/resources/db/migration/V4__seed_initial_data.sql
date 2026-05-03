-- =============================================================================
-- V4__seed_initial_data.sql
-- Seeds: 3 states, 8 roles, default permissions, and default system config.
-- Super Admin is created via ApplicationRunner (SuperAdminSeeder.java)
-- using environment variables — NOT hardcoded here.
-- =============================================================================

-- Seed states
INSERT INTO states (name, code) VALUES
    ('Bihar', 'BR'),
    ('Uttar Pradesh', 'UP'),
    ('Jharkhand', 'JH');

-- Seed roles with hierarchy levels
INSERT INTO roles (name, display_name, hierarchy_level) VALUES
    ('SUPER_ADMIN', 'Super Admin', 1),
    ('SYSTEM_ADMIN', 'System Admin', 2),
    ('STATE_ADMIN', 'State Admin', 3),
    ('DISTRICT_ADMIN', 'District Admin', 4),
    ('BLOCK_ADMIN', 'Block Admin', 5),
    ('RECEPTIONIST', 'Receptionist', 6),
    ('VOLUNTEER', 'Volunteer', 7),
    ('PHARMACIST', 'Pharmacist', 3);

-- Seed permissions for each module
INSERT INTO permissions (module, action, description) VALUES
    -- Users
    ('USERS', 'VIEW', 'View user profiles and lists'),
    ('USERS', 'CREATE', 'Create new users'),
    ('USERS', 'EDIT', 'Edit user information'),
    ('USERS', 'DEACTIVATE', 'Deactivate user accounts'),
    ('USERS', 'REACTIVATE', 'Reactivate deactivated user accounts'),
    -- Patients
    ('PATIENTS', 'VIEW', 'View patient profiles and lists'),
    ('PATIENTS', 'CREATE', 'Create individual patient records'),
    ('PATIENTS', 'BULK_UPLOAD', 'Bulk upload patients from CSV'),
    ('PATIENTS', 'EDIT', 'Edit patient information'),
    -- Health Cards
    ('HEALTH_CARDS', 'VIEW', 'View health card details'),
    ('HEALTH_CARDS', 'ISSUE', 'Issue new health cards'),
    ('HEALTH_CARDS', 'ADD_FAMILY_MEMBER', 'Add family members to a health card'),
    -- Inventory
    ('INVENTORY', 'VIEW_STOCK', 'View stock levels'),
    ('INVENTORY', 'ADD_STOCK', 'Add new stock to central inventory'),
    ('INVENTORY', 'TRANSFER_STOCK', 'Create and manage stock transfers'),
    -- Billing
    ('BILLING', 'VIEW', 'View bills and sales records'),
    ('BILLING', 'CREATE', 'Create new bills'),
    ('BILLING', 'CANCEL', 'Cancel existing bills'),
    -- Reports
    ('REPORTS', 'VIEW', 'View reports and analytics'),
    ('REPORTS', 'DOWNLOAD', 'Download reports as PDF or Excel'),
    -- Commissions
    ('COMMISSIONS', 'VIEW', 'View commission reports'),
    ('COMMISSIONS', 'EDIT_PERCENTAGE', 'Edit commission percentages'),
    -- Notifications
    ('NOTIFICATIONS', 'VIEW', 'View notifications'),
    ('NOTIFICATIONS', 'SEND', 'Send notifications'),
    -- Messaging
    ('MESSAGING', 'SEND_SMS', 'Send SMS messages to patients'),
    ('MESSAGING', 'SEND_EMAIL', 'Send email messages to patients'),
    ('MESSAGING', 'SEND_WHATSAPP', 'Send WhatsApp messages to patients'),
    -- Stores
    ('STORES', 'VIEW', 'View store profiles'),
    ('STORES', 'CREATE', 'Create new stores'),
    ('STORES', 'EDIT', 'Edit store information'),
    -- Geography
    ('GEOGRAPHY', 'VIEW', 'View geographic hierarchy'),
    ('GEOGRAPHY', 'MANAGE', 'Create and edit states, districts, blocks');

-- Seed default system configuration
INSERT INTO system_config (config_key, config_value, description) VALUES
    ('RETURN_WINDOW_DAYS', '7', 'Number of days within which a product can be returned'),
    ('DEFAULT_PAGE_SIZE', '20', 'Default number of items per page in list endpoints'),
    ('MAX_PAGE_SIZE', '100', 'Maximum allowed page size'),
    ('MAX_FAMILY_MEMBERS', '5', 'Maximum number of family members per health card'),
    ('LOW_STOCK_THRESHOLD_DEFAULT', '10', 'Default minimum stock threshold for alerts');
