-- ===========================================
-- MIGRATION: Add payment fields to user_memberships
-- ===========================================
-- Description: Adds total_amount and paid_amount fields to track membership payments
-- Date: 2025-01-XX
-- ===========================================

-- Add total_amount column (precio total de la membresía)
ALTER TABLE user_memberships 
ADD COLUMN IF NOT EXISTS total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- Add paid_amount column (monto pagado hasta ahora)
ALTER TABLE user_memberships 
ADD COLUMN IF NOT EXISTS paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- Update existing memberships: set total_amount from product price
UPDATE user_memberships um
SET total_amount = COALESCE(p.price, 0.00)
FROM products p
WHERE um.product_id = p.id
AND um.total_amount = 0.00;

-- Add comment to columns
COMMENT ON COLUMN user_memberships.total_amount IS 'Precio total de la membresía';
COMMENT ON COLUMN user_memberships.paid_amount IS 'Monto pagado hasta ahora';

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_user_membership_total_amount ON user_memberships(total_amount);
CREATE INDEX IF NOT EXISTS idx_user_membership_paid_amount ON user_memberships(paid_amount);

