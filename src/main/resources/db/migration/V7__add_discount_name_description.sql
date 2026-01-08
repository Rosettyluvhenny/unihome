-- Add name and description columns to discounts table
ALTER TABLE discounts 
ADD COLUMN name VARCHAR(100) NOT NULL DEFAULT 'Unnamed Discount',
ADD COLUMN description TEXT;

-- Remove default after adding column
ALTER TABLE discounts ALTER COLUMN name DROP DEFAULT;
