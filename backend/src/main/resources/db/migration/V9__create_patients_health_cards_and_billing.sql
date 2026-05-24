-- =============================================================================
-- V9__create_patients_health_cards_and_billing.sql
-- Creates tables: hospitals, patients, health_cards, health_card_members,
-- bills, bill_items, schemes, commission_config, commission_entries.
-- Seeds initial commission configs.
-- =============================================================================

CREATE TABLE hospitals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address TEXT,
    phone VARCHAR(15),
    contact_person VARCHAR(150),
    state_id BIGINT NOT NULL,
    district_id BIGINT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hospitals_state FOREIGN KEY (state_id) REFERENCES states(id),
    CONSTRAINT fk_hospitals_district FOREIGN KEY (district_id) REFERENCES districts(id),
    INDEX idx_hospitals_state_id (state_id),
    INDEX idx_hospitals_district_id (district_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    age INT,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(150),
    address TEXT,
    state_id BIGINT NOT NULL,
    district_id BIGINT NOT NULL,
    block_id BIGINT NOT NULL,
    store_id BIGINT,
    hospital_id BIGINT,
    messaging_pref ENUM('SMS', 'EMAIL', 'WHATSAPP', 'ALL') NOT NULL DEFAULT 'ALL',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_patients_state FOREIGN KEY (state_id) REFERENCES states(id),
    CONSTRAINT fk_patients_district FOREIGN KEY (district_id) REFERENCES districts(id),
    CONSTRAINT fk_patients_block FOREIGN KEY (block_id) REFERENCES blocks(id),
    CONSTRAINT fk_patients_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_patients_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_patients_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_patients_phone (phone),
    INDEX idx_patients_state_id (state_id),
    INDEX idx_patients_district_id (district_id),
    INDEX idx_patients_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE health_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_number VARCHAR(30) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL UNIQUE,
    store_id BIGINT NOT NULL,
    issued_by BIGINT NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_health_cards_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_health_cards_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_health_cards_issued_by FOREIGN KEY (issued_by) REFERENCES users(id),
    INDEX idx_health_cards_number (card_number),
    INDEX idx_health_cards_patient_id (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE health_card_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    health_card_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    relation VARCHAR(50) NOT NULL,
    age INT NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_members_card FOREIGN KEY (health_card_id) REFERENCES health_cards(id) ON DELETE CASCADE,
    INDEX idx_card_members_card_id (health_card_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(30) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    health_card_id BIGINT,
    total_mrp DECIMAL(12, 2) NOT NULL,
    total_ask_price DECIMAL(12, 2) NOT NULL,
    total_gst DECIMAL(10, 2) NOT NULL,
    total_discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    net_amount DECIMAL(12, 2) NOT NULL,
    total_savings DECIMAL(12, 2) NOT NULL,
    payment_mode ENUM('CASH', 'UPI', 'CARD') NOT NULL,
    status ENUM('ACTIVE', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    cancel_reason TEXT,
    cancelled_by BIGINT,
    created_by BIGINT NOT NULL,
    bill_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bills_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_bills_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_bills_card FOREIGN KEY (health_card_id) REFERENCES health_cards(id),
    CONSTRAINT fk_bills_cancelled_by FOREIGN KEY (cancelled_by) REFERENCES users(id),
    CONSTRAINT fk_bills_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_bills_number (bill_number),
    INDEX idx_bills_store_id (store_id),
    INDEX idx_bills_patient_id (patient_id),
    INDEX idx_bills_date (bill_date),
    INDEX idx_bills_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bill_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    mrp DECIMAL(10, 2) NOT NULL,
    ask_price DECIMAL(10, 2) NOT NULL,
    gst_amount DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(10, 2) NOT NULL,
    return_status ENUM('NONE', 'REQUESTED', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'NONE',
    return_quantity INT NOT NULL DEFAULT 0,
    return_reason TEXT,
    CONSTRAINT fk_bill_items_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_bill_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_bill_items_bill_id (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE schemes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    discount_type ENUM('PERCENTAGE', 'FLAT') NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    category_id BIGINT,
    state_id BIGINT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_schemes_category FOREIGN KEY (category_id) REFERENCES product_categories(id),
    CONSTRAINT fk_schemes_state FOREIGN KEY (state_id) REFERENCES states(id),
    CONSTRAINT fk_schemes_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_schemes_status (status),
    INDEX idx_schemes_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE commission_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL UNIQUE,
    percentage DECIMAL(5, 2) NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comm_config_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_comm_config_user FOREIGN KEY (updated_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE commission_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    month VARCHAR(7) NOT NULL,
    status ENUM('CALCULATED', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'CALCULATED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comm_entries_bill FOREIGN KEY (bill_id) REFERENCES bills(id),
    CONSTRAINT fk_comm_entries_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_comm_entries_role FOREIGN KEY (role_id) REFERENCES roles(id),
    INDEX idx_comm_entries_user_id (user_id),
    INDEX idx_comm_entries_month (month),
    INDEX idx_comm_entries_bill_id (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed default commission percentages (Role IDs map to seeded V4 values)
-- SUPER_ADMIN = 1, STATE_ADMIN = 3, DISTRICT_ADMIN = 4, BLOCK_ADMIN = 5
INSERT INTO commission_config (role_id, percentage, updated_by) VALUES
    (1, 2.00, NULL),
    (3, 3.00, NULL),
    (4, 4.00, NULL),
    (5, 5.00, NULL);
