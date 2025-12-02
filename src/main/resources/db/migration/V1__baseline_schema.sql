-- =============================================
-- FIT & FLEX - BASELINE MIGRATION
-- =============================================
-- This migration represents the initial database schema.
-- Created from existing JPA entities.
-- =============================================

-- =============================================
-- ROLES TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_role_name ON roles(name);

-- =============================================
-- BRANCHES TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(150),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_branch_name ON branches(name);
CREATE INDEX IF NOT EXISTS idx_branch_city ON branches(city);

-- =============================================
-- USERS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    gender VARCHAR(10),
    birth_date DATE,
    profile_image_url VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    role_id BIGINT NOT NULL,
    branch_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_user_branch FOREIGN KEY (branch_id) REFERENCES branches(id)
);

CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_role ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_user_branch ON users(branch_id);

-- =============================================
-- CLASSES TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS classes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    capacity INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    branch_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_class_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_class_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_class_branch ON classes(branch_id);
CREATE INDEX IF NOT EXISTS idx_class_created_by ON classes(created_by);

-- =============================================
-- CLASS_SCHEDULE_PATTERNS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS class_schedule_patterns (
    id BIGSERIAL PRIMARY KEY,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    recurrent BOOLEAN NOT NULL DEFAULT FALSE,
    class_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_pattern_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_pattern_class ON class_schedule_patterns(class_id);
CREATE INDEX IF NOT EXISTS idx_pattern_day ON class_schedule_patterns(day_of_week);

-- =============================================
-- SCHEDULES TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS schedules (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    class_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_schedule_class FOREIGN KEY (class_id) REFERENCES classes(id)
);

CREATE INDEX IF NOT EXISTS idx_schedule_class ON schedules(class_id);
CREATE INDEX IF NOT EXISTS idx_schedule_active ON schedules(active);
CREATE INDEX IF NOT EXISTS idx_schedule_times ON schedules(start_time, end_time);

-- =============================================
-- RESERVATIONS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    reservation_date TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reservation_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id)
);

CREATE INDEX IF NOT EXISTS idx_reservation_user ON reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_reservation_schedule ON reservations(schedule_id);
CREATE INDEX IF NOT EXISTS idx_reservation_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_reservation_date ON reservations(reservation_date);

-- =============================================
-- CLASS_SUBSCRIPTIONS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS class_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    date DATE,
    day_of_week INTEGER NOT NULL,
    recurrent BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_subscription_class FOREIGN KEY (class_id) REFERENCES classes(id),
    CONSTRAINT uk_class_subscription_user_class_day_date_time 
        UNIQUE (user_id, class_id, day_of_week, date, start_time, end_time)
);

CREATE INDEX IF NOT EXISTS idx_class_subscription_user ON class_subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_class_subscription_class ON class_subscriptions(class_id);
CREATE INDEX IF NOT EXISTS idx_class_subscription_active ON class_subscriptions(active);
CREATE INDEX IF NOT EXISTS idx_class_subscription_day ON class_subscriptions(day_of_week);

-- =============================================
-- PRODUCTS TABLE (Memberships)
-- =============================================
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(50),
    sku VARCHAR(20),
    membership_type VARCHAR(20),
    price NUMERIC(10,2) NOT NULL,
    duration_days INTEGER NOT NULL,
    max_users INTEGER,
    number_of_classes INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    auto_renewal BOOLEAN DEFAULT FALSE,
    trial_period_days INTEGER,
    image_url VARCHAR(500),
    benefits VARCHAR(2000),
    features VARCHAR(2000),
    branch_id BIGINT NOT NULL,
    class_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_product_branch FOREIGN KEY (branch_id) REFERENCES branches(id),
    CONSTRAINT fk_product_class FOREIGN KEY (class_id) REFERENCES classes(id)
);

CREATE INDEX IF NOT EXISTS idx_product_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_product_branch ON products(branch_id);
CREATE INDEX IF NOT EXISTS idx_product_class ON products(class_id);
CREATE INDEX IF NOT EXISTS idx_product_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_product_active ON products(active);

-- =============================================
-- USER_MEMBERSHIPS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS user_memberships (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(1000),
    total_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    paid_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    assigned_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_user_membership_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_membership_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_user_membership_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_user_membership_user ON user_memberships(user_id);
CREATE INDEX IF NOT EXISTS idx_user_membership_product ON user_memberships(product_id);
CREATE INDEX IF NOT EXISTS idx_user_membership_assigned_by ON user_memberships(assigned_by);
CREATE INDEX IF NOT EXISTS idx_user_membership_status ON user_memberships(status);
CREATE INDEX IF NOT EXISTS idx_user_membership_active ON user_memberships(active);
CREATE INDEX IF NOT EXISTS idx_user_membership_dates ON user_memberships(start_date, end_date);

-- =============================================
-- PAYMENTS TABLE
-- =============================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_date TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100) UNIQUE,
    gateway_reference VARCHAR(100),
    gateway_response VARCHAR(1000),
    description VARCHAR(500),
    failure_reason VARCHAR(500),
    refund_amount NUMERIC(10,2),
    refund_date TIMESTAMP,
    refund_reason VARCHAR(500),
    user_id BIGINT NOT NULL,
    reservation_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_payment_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

CREATE INDEX IF NOT EXISTS idx_payment_user ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_reservation ON payments(reservation_id);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payment_date ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_payment_transaction_id ON payments(transaction_id);

-- =============================================
-- INSERT DEFAULT ROLES
-- =============================================
INSERT INTO roles (name, description, created_at) VALUES
    ('SUPER_ADMIN', 'Super administrador del sistema', NOW()),
    ('BRANCH_ADMIN', 'Administrador de sucursal', NOW()),
    ('INSTRUCTOR', 'Instructor de clases', NOW()),
    ('USER', 'Usuario regular', NOW())
ON CONFLICT (name) DO NOTHING;

