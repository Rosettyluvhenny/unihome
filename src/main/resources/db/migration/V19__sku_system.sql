SET search_path TO unihome;

-- =============================================
-- 1. Attribute types per furniture (e.g. Color, Size)
-- =============================================
CREATE TABLE IF NOT EXISTS furniture_attribute_types (
    attribute_type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    furniture_id      UUID         NOT NULL,
    name              VARCHAR(100) NOT NULL,
    display_order     INT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_attr_type_furniture FOREIGN KEY (furniture_id) REFERENCES furniture (furniture_id) ON DELETE CASCADE,
    CONSTRAINT uq_attr_type_per_furniture UNIQUE (furniture_id, name)
);

CREATE INDEX IF NOT EXISTS idx_attr_type_furniture ON furniture_attribute_types (furniture_id);

-- =============================================
-- 2. SKUs per furniture
-- =============================================
CREATE TABLE IF NOT EXISTS furniture_skus (
    sku_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    furniture_id UUID           NOT NULL,
    sku_code     VARCHAR(100)   NOT NULL UNIQUE,
    price        DECIMAL(12, 2) NOT NULL,
    final_price  DECIMAL(12, 2),
    stock        INT            NOT NULL DEFAULT 0,
    status       VARCHAR(50)    NOT NULL DEFAULT 'AVAILABLE',
    has_discount BOOLEAN        NOT NULL DEFAULT FALSE,
    image_url    VARCHAR(1024),
    created_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at   TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_sku_furniture FOREIGN KEY (furniture_id) REFERENCES furniture (furniture_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_sku_furniture ON furniture_skus (furniture_id);
CREATE INDEX IF NOT EXISTS idx_sku_status ON furniture_skus (status);

-- =============================================
-- 3. Attribute values per SKU
-- =============================================
CREATE TABLE IF NOT EXISTS sku_attribute_values (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku_id            UUID         NOT NULL,
    attribute_type_id UUID         NOT NULL,
    value             VARCHAR(255) NOT NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at        TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_sav_sku FOREIGN KEY (sku_id) REFERENCES furniture_skus (sku_id) ON DELETE CASCADE,
    CONSTRAINT fk_sav_attr_type FOREIGN KEY (attribute_type_id) REFERENCES furniture_attribute_types (attribute_type_id) ON DELETE CASCADE,
    CONSTRAINT uq_sku_attr UNIQUE (sku_id, attribute_type_id)
);

CREATE INDEX IF NOT EXISTS idx_sav_sku ON sku_attribute_values (sku_id);

-- =============================================
-- 4. Alter cart_items: add sku_id
-- =============================================
ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS sku_id UUID;

-- =============================================
-- 6. Alter order_items: add sku_id
-- =============================================
ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS sku_id UUID;

-- =============================================
-- 7. Create default SKU for each existing furniture
-- =============================================
INSERT INTO furniture_skus (sku_id, furniture_id, sku_code, price, final_price, stock, status, has_discount, created_at, updated_at)
SELECT gen_random_uuid(),
       f.furniture_id,
       'DEFAULT-' || LEFT(f.furniture_id::TEXT, 8),
       f.price,
       f.final_price,
       f.stock,
       f.status,
       f.has_discount,
       NOW(), NOW()
FROM furniture f
WHERE NOT EXISTS (
    SELECT 1 FROM furniture_skus s WHERE s.furniture_id = f.furniture_id
);

-- =============================================
-- 8. Backfill sku_id in cart_items
-- =============================================
UPDATE cart_items ci
SET sku_id = (
    SELECT s.sku_id
    FROM furniture_skus s
    WHERE s.furniture_id = ci.furniture_id
    LIMIT 1
)
WHERE ci.sku_id IS NULL;

-- =============================================
-- 9. Backfill sku_id in order_items
-- =============================================
UPDATE order_items oi
SET sku_id = (
    SELECT s.sku_id
    FROM furniture_skus s
    WHERE s.furniture_id = oi.furniture_id
    LIMIT 1
)
WHERE oi.sku_id IS NULL;

-- =============================================
-- 10. Add FK constraints now that data is backfilled
-- =============================================
ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_sku FOREIGN KEY (sku_id) REFERENCES furniture_skus (sku_id) ON DELETE CASCADE;

ALTER TABLE order_items
    ADD CONSTRAINT fk_order_items_sku FOREIGN KEY (sku_id) REFERENCES furniture_skus (sku_id) ON DELETE CASCADE;

-- =============================================
-- 11. Replace old unique on cart_items (cart_id, furniture_id) with (cart_id, sku_id)
-- =============================================
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS uk_cart_items_cart_furniture;
ALTER TABLE cart_items ADD CONSTRAINT uk_cart_items_cart_sku UNIQUE (cart_id, sku_id);

-- =============================================
-- 12. Seed SKU data for the 3 existing furniture items
-- =============================================

-- Attribute types for Oslo Fabric Sofa
INSERT INTO furniture_attribute_types (attribute_type_id, furniture_id, name, display_order)
VALUES
    ('a1000001-0001-0001-0001-000000000001', 'd5ebf19f-b227-4f52-8f7a-4d2c04a30f01', 'Màu sắc', 0),
    ('a1000001-0001-0001-0001-000000000002', 'd5ebf19f-b227-4f52-8f7a-4d2c04a30f01', 'Chất liệu', 1)
ON CONFLICT DO NOTHING;

-- Attribute types for Nordic Oak Bed Frame
INSERT INTO furniture_attribute_types (attribute_type_id, furniture_id, name, display_order)
VALUES
    ('a2000001-0001-0001-0001-000000000001', '1c5f4f46-6fcd-4ec0-90a7-734f1a9b3bb2', 'Màu sắc', 0),
    ('a2000001-0001-0001-0001-000000000002', '1c5f4f46-6fcd-4ec0-90a7-734f1a9b3bb2', 'Kích thước', 1)
ON CONFLICT DO NOTHING;

-- Attribute types for ErgoLift Standing Desk
INSERT INTO furniture_attribute_types (attribute_type_id, furniture_id, name, display_order)
VALUES
    ('a3000001-0001-0001-0001-000000000001', '9a1e5e8d-8e9b-4f5f-baf3-605d53a5ca21', 'Màu sắc', 0)
ON CONFLICT DO NOTHING;

-- Delete default SKUs (they will be replaced by specific ones)
DELETE FROM furniture_skus WHERE sku_code LIKE 'DEFAULT-%';

-- SKUs for Oslo Fabric Sofa (d5ebf19f...)
INSERT INTO furniture_skus (sku_id, furniture_id, sku_code, price, final_price, stock, status, has_discount)
VALUES
    ('b1000001-0001-0001-0001-000000000001', 'd5ebf19f-b227-4f52-8f7a-4d2c04a30f01',
     'SOFA-GHI-COTTON', 15000000.00, 15000000.00, 5, 'AVAILABLE', FALSE),
    ('b1000001-0001-0001-0001-000000000002', 'd5ebf19f-b227-4f52-8f7a-4d2c04a30f01',
     'SOFA-NAU-COTTON', 15000000.00, 15000000.00, 4, 'AVAILABLE', FALSE),
    ('b1000001-0001-0001-0001-000000000003', 'd5ebf19f-b227-4f52-8f7a-4d2c04a30f01',
     'SOFA-GHI-LINEN', 16500000.00, 16500000.00, 3, 'AVAILABLE', FALSE)
ON CONFLICT (sku_code) DO NOTHING;

-- Attribute values for Oslo Fabric Sofa SKUs
INSERT INTO sku_attribute_values (sku_id, attribute_type_id, value)
VALUES
    ('b1000001-0001-0001-0001-000000000001', 'a1000001-0001-0001-0001-000000000001', 'Xám ghi'),
    ('b1000001-0001-0001-0001-000000000001', 'a1000001-0001-0001-0001-000000000002', 'Cotton'),
    ('b1000001-0001-0001-0001-000000000002', 'a1000001-0001-0001-0001-000000000001', 'Nâu'),
    ('b1000001-0001-0001-0001-000000000002', 'a1000001-0001-0001-0001-000000000002', 'Cotton'),
    ('b1000001-0001-0001-0001-000000000003', 'a1000001-0001-0001-0001-000000000001', 'Xám ghi'),
    ('b1000001-0001-0001-0001-000000000003', 'a1000001-0001-0001-0001-000000000002', 'Linen')
ON CONFLICT DO NOTHING;

-- SKUs for Nordic Oak Bed Frame (1c5f4f46...)
INSERT INTO furniture_skus (sku_id, furniture_id, sku_code, price, final_price, stock, status, has_discount)
VALUES
    ('b2000001-0001-0001-0001-000000000001', '1c5f4f46-6fcd-4ec0-90a7-734f1a9b3bb2',
     'BED-WALNUT-QUEEN', 9800000.00, 9200000.00, 10, 'AVAILABLE', FALSE),
    ('b2000001-0001-0001-0001-000000000002', '1c5f4f46-6fcd-4ec0-90a7-734f1a9b3bb2',
     'BED-WALNUT-KING', 11500000.00, 11500000.00, 5, 'AVAILABLE', FALSE),
    ('b2000001-0001-0001-0001-000000000003', '1c5f4f46-6fcd-4ec0-90a7-734f1a9b3bb2',
     'BED-OAK-QUEEN', 9800000.00, 9200000.00, 5, 'AVAILABLE', FALSE)
ON CONFLICT (sku_code) DO NOTHING;

-- Attribute values for Nordic Oak Bed Frame SKUs
INSERT INTO sku_attribute_values (sku_id, attribute_type_id, value)
VALUES
    ('b2000001-0001-0001-0001-000000000001', 'a2000001-0001-0001-0001-000000000001', 'Walnut'),
    ('b2000001-0001-0001-0001-000000000001', 'a2000001-0001-0001-0001-000000000002', 'Queen'),
    ('b2000001-0001-0001-0001-000000000002', 'a2000001-0001-0001-0001-000000000001', 'Walnut'),
    ('b2000001-0001-0001-0001-000000000002', 'a2000001-0001-0001-0001-000000000002', 'King'),
    ('b2000001-0001-0001-0001-000000000003', 'a2000001-0001-0001-0001-000000000001', 'Oak'),
    ('b2000001-0001-0001-0001-000000000003', 'a2000001-0001-0001-0001-000000000002', 'Queen')
ON CONFLICT DO NOTHING;

-- SKUs for ErgoLift Standing Desk (9a1e5e8d...)
INSERT INTO furniture_skus (sku_id, furniture_id, sku_code, price, final_price, stock, status, has_discount)
VALUES
    ('b3000001-0001-0001-0001-000000000001', '9a1e5e8d-8e9b-4f5f-baf3-605d53a5ca21',
     'DESK-TRANG', 12500000.00, 12500000.00, 8, 'AVAILABLE', FALSE),
    ('b3000001-0001-0001-0001-000000000002', '9a1e5e8d-8e9b-4f5f-baf3-605d53a5ca21',
     'DESK-DEN', 12500000.00, 12500000.00, 7, 'AVAILABLE', FALSE)
ON CONFLICT (sku_code) DO NOTHING;

-- Attribute values for ErgoLift Standing Desk SKUs
INSERT INTO sku_attribute_values (sku_id, attribute_type_id, value)
VALUES
    ('b3000001-0001-0001-0001-000000000001', 'a3000001-0001-0001-0001-000000000001', 'Trắng'),
    ('b3000001-0001-0001-0001-000000000002', 'a3000001-0001-0001-0001-000000000001', 'Đen')
ON CONFLICT DO NOTHING;

-- =============================================
-- 13. Update furniture.stock to be SUM of its SKU stocks (for display)
-- =============================================
UPDATE furniture f
SET stock = COALESCE((SELECT SUM(s.stock) FROM furniture_skus s WHERE s.furniture_id = f.furniture_id), 0);
