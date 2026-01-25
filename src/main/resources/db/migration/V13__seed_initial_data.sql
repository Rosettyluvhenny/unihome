SET search_path TO unihome;

-- Seed core categories
INSERT INTO categories (category_id, name, created_at, updated_at) VALUES
    ('7f89b4c9-8c3b-4ec3-9f4c-c1d1c9d3f8a1', 'Living Room', NOW(), NOW()),
    ('a4dcbdb2-3744-4c9a-8c4b-21f7e8f6e932', 'Bedroom', NOW(), NOW()),
    ('c68c8e2f-6acf-4c3c-8c34-41dff18a2c54', 'Home Office', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Seed demo users with coordinates
INSERT INTO users (id, full_name, email, password, status, role_name, created_at, updated_at, address, latitude, longitude, phone, image) VALUES
    ('11111111-1111-1111-1111-111111111111', 'System Admin', 'admin@unihome.com', '$2a$10$DowSDWn/7eQC7KVio9oi0e5Qn2ATqNnS/xPbbGr3HdWu3UwG2n7Aa', 'ACTIVE', 'ADMIN', NOW(), NOW(), '123 Admin Street, Thu Duc City, HCMC', 10.875056, 106.800667, '0900000001', NULL),
    ('22222222-2222-2222-2222-222222222222', 'Nguyen Van A', 'customer@unihome.com', '$2a$10$DowSDWn/7eQC7KVio9oi0e5Qn2ATqNnS/xPbbGr3HdWu3UwG2n7Aa', 'ACTIVE', 'CUSTOMER', NOW(), NOW(), '45 Nguyen Thi Minh Khai, District 1, HCMC', 10.776889, 106.700806, '0900000002', NULL)
ON CONFLICT (email) DO NOTHING;

-- Seed showcase furniture referencing categories
INSERT INTO furniture (furniture_id, category_id, name, price, final_price, stock, status, has_discount, created_at, updated_at) VALUES
    ('d5ebf19f-b227-4f52-8f7a-4d2c04a30f01', (SELECT category_id FROM categories WHERE name = 'Living Room'), 'Oslo Fabric Sofa', 15000000.00, 15000000.00, 12, 'AVAILABLE', FALSE, NOW(), NOW()),
    ('1c5f4f46-6fcd-4ec0-90a7-734f1a9b3bb2', (SELECT category_id FROM categories WHERE name = 'Bedroom'), 'Nordic Oak Bed Frame', 9800000.00, 9200000.00, 20, 'AVAILABLE', FALSE, NOW(), NOW()),
    ('9a1e5e8d-8e9b-4f5f-baf3-605d53a5ca21', (SELECT category_id FROM categories WHERE name = 'Home Office'), 'ErgoLift Standing Desk', 12500000.00, 12500000.00, 15, 'AVAILABLE', FALSE, NOW(), NOW())
ON CONFLICT (furniture_id) DO NOTHING;
