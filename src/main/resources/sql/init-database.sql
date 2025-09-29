-- ===========================================
-- FIT & FLEX - DATABASE INITIALIZATION SCRIPT
-- ===========================================

-- Create database (run this as superuser)
-- CREATE DATABASE fitandflex_db;
-- CREATE USER fitandflex_user WITH PASSWORD 'fitandflex_password';
-- GRANT ALL PRIVILEGES ON DATABASE fitandflex_db TO fitandflex_user;

-- Connect to the database
-- \c fitandflex_db;

-- ===========================================
-- CREATE EXTENSIONS
-- ===========================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===========================================
-- CREATE ROLES TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on roles
CREATE INDEX IF NOT EXISTS idx_role_name ON roles(name);

-- ===========================================
-- CREATE BRANCHES TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(150),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes on branches
CREATE INDEX IF NOT EXISTS idx_branch_name ON branches(name);
CREATE INDEX IF NOT EXISTS idx_branch_city ON branches(city);

-- ===========================================
-- CREATE USERS TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    gender VARCHAR(10),
    profile_image_url VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    role_id BIGINT NOT NULL,
    branch_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_user_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);

-- Create indexes on users
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_role ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_user_branch ON users(branch_id);

-- ===========================================
-- CREATE CLASES TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS clases (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(50),
    difficulty VARCHAR(20),
    scheduled_date TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    max_capacity INTEGER NOT NULL,
    current_bookings INTEGER DEFAULT 0,
    price DECIMAL(10,2) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    is_cancelled BOOLEAN DEFAULT FALSE,
    branch_id BIGINT NOT NULL,
    instructor_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_clase_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_clase_instructor FOREIGN KEY (instructor_id) REFERENCES users(id)
);

-- Create indexes on clases
CREATE INDEX IF NOT EXISTS idx_clase_name ON clases(name);
CREATE INDEX IF NOT EXISTS idx_clase_branch ON clases(branch_id);
CREATE INDEX IF NOT EXISTS idx_clase_schedule ON clases(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_clase_instructor ON clases(instructor_id);

-- ===========================================
-- CREATE RESERVATIONS TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    reservation_date TIMESTAMP NOT NULL,
    reservation_code VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    notes VARCHAR(500),
    cancellation_reason VARCHAR(500),
    cancelled_at TIMESTAMP,
    user_id BIGINT NOT NULL,
    clase_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reservation_clase FOREIGN KEY (clase_id) REFERENCES clases(id),
    CONSTRAINT uk_reservation_user_clase UNIQUE (user_id, clase_id)
);

-- Create indexes on reservations
CREATE INDEX IF NOT EXISTS idx_reservation_user ON reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_reservation_clase ON reservations(clase_id);
CREATE INDEX IF NOT EXISTS idx_reservation_date ON reservations(reservation_date);
CREATE INDEX IF NOT EXISTS idx_reservation_status ON reservations(status);

-- ===========================================
-- CREATE PAYMENTS TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100) UNIQUE,
    gateway_reference VARCHAR(100),
    gateway_response VARCHAR(1000),
    description VARCHAR(500),
    failure_reason VARCHAR(500),
    refund_amount DECIMAL(10,2),
    refund_date TIMESTAMP,
    refund_reason VARCHAR(500),
    user_id BIGINT NOT NULL,
    reservation_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payment_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

-- Create indexes on payments
CREATE INDEX IF NOT EXISTS idx_payment_user ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_reservation ON payments(reservation_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_date ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_payment_transaction_id ON payments(transaction_id);

-- ===========================================
-- CREATE PRODUCTS TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(50),
    sku VARCHAR(20) UNIQUE,
    brand VARCHAR(100),
    size VARCHAR(20),
    color VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2),
    stock_quantity INTEGER,
    min_stock_level INTEGER,
    max_stock_level INTEGER,
    active BOOLEAN DEFAULT TRUE,
    is_digital BOOLEAN DEFAULT FALSE,
    requires_approval BOOLEAN DEFAULT FALSE,
    is_subscription BOOLEAN DEFAULT FALSE,
    subscription_duration_days INTEGER,
    image_url VARCHAR(500),
    tags VARCHAR(500),
    weight_grams INTEGER,
    dimensions VARCHAR(50),
    branch_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);

-- Create indexes on products
CREATE INDEX IF NOT EXISTS idx_product_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_product_branch ON products(branch_id);
CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_product_active ON products(active);

-- ===========================================
-- INSERT DEFAULT ROLES
-- ===========================================
INSERT INTO roles (name, description) VALUES
('SUPER_ADMIN', 'Super administrador del sistema con acceso completo'),
('BRANCH_ADMIN', 'Administrador de sucursal con acceso limitado a su sucursal'),
('USER', 'Usuario regular del sistema'),
('INSTRUCTOR', 'Instructor de yoga con acceso a sus clases')
ON CONFLICT (name) DO NOTHING;

-- ===========================================
-- INSERT DEFAULT BRANCH (for testing)
-- ===========================================
INSERT INTO branches (name, address, city, state, country, phone, email) VALUES
('Fit & Flex Quito Norte', 'Av. 6 de Diciembre N12-123', 'Quito', 'Pichincha', 'Ecuador', '+593-2-1234567', 'quito.norte@fitandflex.com'),
('Fit & Flex Guayaquil Centro', 'Av. 9 de Octubre 123-45', 'Guayaquil', 'Guayas', 'Ecuador', '+593-4-9876543', 'guayaquil.centro@fitandflex.com')
ON CONFLICT (name) DO NOTHING;

-- ===========================================
-- CREATE TRIGGERS FOR UPDATED_AT
-- ===========================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to all tables
CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON roles FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_branches_updated_at BEFORE UPDATE ON branches FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_clases_updated_at BEFORE UPDATE ON clases FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_reservations_updated_at BEFORE UPDATE ON reservations FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
