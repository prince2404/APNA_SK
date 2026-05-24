-- =============================================================================
-- V8__create_products_and_inventory.sql
-- Creates tables: product_categories, products, stock_central, transfer_orders,
-- transfer_order_items, stock_store, stock_adjustments, stock_requests.
-- Seeds initial categories and new product permissions.
-- =============================================================================

CREATE TABLE product_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    brand VARCHAR(100),
    category_id BIGINT NOT NULL,
    hsn_code VARCHAR(20),
    mrp DECIMAL(10, 2) NOT NULL,
    ask_price DECIMAL(10, 2) NOT NULL,
    gst_percentage DECIMAL(5, 2) NOT NULL,
    min_stock_threshold INT NOT NULL DEFAULT 10,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES product_categories(id),
    INDEX idx_products_name (name),
    INDEX idx_products_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE stock_central (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    manufacturing_date DATE,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL,
    received_by BIGINT NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('AVAILABLE', 'TRANSFERRED', 'EXPIRED') NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_central_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_stock_central_received_by FOREIGN KEY (received_by) REFERENCES users(id),
    INDEX idx_stock_central_product_id (product_id),
    INDEX idx_stock_central_batch_number (batch_number),
    INDEX idx_stock_central_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transfer_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_number VARCHAR(30) NOT NULL UNIQUE,
    store_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    status ENUM('PENDING', 'IN_TRANSIT', 'RECEIVED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    confirmed_by BIGINT,
    confirmed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfer_orders_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_transfer_orders_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_transfer_orders_confirmed_by FOREIGN KEY (confirmed_by) REFERENCES users(id),
    INDEX idx_transfer_orders_store_id (store_id),
    INDEX idx_transfer_orders_created_by (created_by),
    INDEX idx_transfer_orders_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transfer_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_transfer_items_order FOREIGN KEY (transfer_order_id) REFERENCES transfer_orders(id),
    CONSTRAINT fk_transfer_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_transfer_items_order_id (transfer_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE stock_store (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity INT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_store_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_stock_store_product FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_store_product_batch (store_id, product_id, batch_number),
    INDEX idx_stock_store_store_id (store_id),
    INDEX idx_stock_store_product_id (product_id),
    INDEX idx_stock_store_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE stock_adjustments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    adjustment_type ENUM('DAMAGE', 'RETURN', 'CORRECTION', 'EXPIRY') NOT NULL,
    quantity_change INT NOT NULL,
    reason TEXT,
    adjusted_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_adj_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_stock_adj_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_stock_adj_user FOREIGN KEY (adjusted_by) REFERENCES users(id),
    INDEX idx_stock_adj_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE stock_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    store_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity_requested INT NOT NULL,
    urgency ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'FULFILLED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    requested_by BIGINT NOT NULL,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_req_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT fk_stock_req_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_stock_req_user FOREIGN KEY (requested_by) REFERENCES users(id),
    CONSTRAINT fk_stock_req_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id),
    INDEX idx_stock_req_store_id (store_id),
    INDEX idx_stock_req_product_id (product_id),
    INDEX idx_stock_req_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed categories
INSERT INTO product_categories (name, status) VALUES
    ('Medicine', 'ACTIVE'),
    ('Baby Food', 'ACTIVE'),
    ('Cosmetics', 'ACTIVE'),
    ('Other', 'ACTIVE');

-- Seed permissions
INSERT INTO permissions (module, action, description) VALUES
    ('PRODUCTS', 'VIEW', 'View products catalogue'),
    ('PRODUCTS', 'CREATE', 'Create new products in catalogue'),
    ('PRODUCTS', 'EDIT', 'Edit existing catalog products'),
    ('PRODUCTS', 'TOGGLE', 'Toggle product status (active/inactive)');
