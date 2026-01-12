-- V1__initial_schema.sql
-- Create UUID extension for PostgreSQL (if using PostgreSQL)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- User-Roles mapping
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);

-- Products table
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product Variants table
CREATE TABLE product_variants (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    product_id UUID NOT NULL,
    sku VARCHAR(255) NOT NULL UNIQUE,
    attributes TEXT,
    price DECIMAL(10,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Inventory table
CREATE TABLE inventories (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    product_variant_id UUID NOT NULL UNIQUE,
    quantity_available INT NOT NULL DEFAULT 0,
    quantity_reserved INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id) ON DELETE CASCADE
);

-- Inventory Reservations table
CREATE TABLE inventory_reservations (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    product_variant_id UUID NOT NULL,
    order_id UUID,
    quantity INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Orders table
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    user_id UUID,
    total_amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(50) NOT NULL,
    tracking_id VARCHAR(255) NOT NULL UNIQUE,
    shipping_address TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order Items table
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    order_id UUID NOT NULL,
    product_variant_id UUID NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id)
);

-- Order Status History table
CREATE TABLE order_status_histories (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    order_id UUID NOT NULL,
    from_status VARCHAR(50) NOT NULL,
    to_status VARCHAR(50) NOT NULL,
    reason TEXT,
    changed_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Payment Transactions table
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    order_id UUID NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    payment_details TEXT,
    failure_reason TEXT,
    provider_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- Cart Items table
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    user_id UUID NOT NULL,
    product_variant_id UUID NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_variant_id) REFERENCES product_variants(id)
);

-- Create indexes for better query performance
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_is_active ON products(is_active);
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);
CREATE INDEX idx_inventory_product_variant_id ON inventories(product_variant_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_tracking_id ON orders(tracking_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_payment_transactions_order_id ON payment_transactions(order_id);
CREATE INDEX idx_payment_transactions_status ON payment_transactions(status);
CREATE INDEX idx_cart_items_user_id ON cart_items(user_id);
CREATE INDEX idx_categories_slug ON categories(slug);

