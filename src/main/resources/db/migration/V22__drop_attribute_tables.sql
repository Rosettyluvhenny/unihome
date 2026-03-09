-- Remove attribute system (not needed for this project)
-- Drop sku_attribute_values first (has FK to furniture_attribute_types)
DROP TABLE IF EXISTS unihome.sku_attribute_values CASCADE;

-- Drop furniture_attribute_types
DROP TABLE IF EXISTS unihome.furniture_attribute_types CASCADE;
