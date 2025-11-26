-- ===========================================
-- MIGRATION: Add class_id field to products
-- ===========================================
-- Description: Adds class_id field to associate a class with a product
-- Date: 2025-01-XX
-- ===========================================

-- Add class_id column (clase asociada al producto)
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS class_id BIGINT;

-- Add foreign key constraint
ALTER TABLE products
ADD CONSTRAINT fk_product_class 
FOREIGN KEY (class_id) REFERENCES classes(id)
ON DELETE SET NULL;

-- Add comment to column
COMMENT ON COLUMN products.class_id IS 'ID de la clase asociada al producto (opcional)';

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_product_class ON products(class_id);

