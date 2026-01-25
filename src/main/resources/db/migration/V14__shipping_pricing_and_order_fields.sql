SET search_path TO unihome;

CREATE TABLE IF NOT EXISTS shipping_price_distance (
    id SERIAL PRIMARY KEY,
    min_distance_km INT NOT NULL,
    max_distance_km INT,
    price INT NOT NULL,
    price_per_km INT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shipping_distance_bounds
    ON shipping_price_distance(min_distance_km, max_distance_km);

INSERT INTO shipping_price_distance (min_distance_km, max_distance_km, price)
VALUES
    (0, 5, 50000),
    (6, 10, 80000),
    (11, 20, 120000),
    (21, 30, 180000),
    (31, 50, 250000),
    (51, 80, 350000),
    (81, 120, 500000)
ON CONFLICT DO NOTHING;

INSERT INTO shipping_price_distance (min_distance_km, max_distance_km, price, price_per_km)
VALUES (121, NULL, 500000, 5000)
ON CONFLICT DO NOTHING;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS shipping_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS distance_km DECIMAL(8, 2),
    ADD COLUMN IF NOT EXISTS free_shipping_applied BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS shipping_full_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS shipping_phone VARCHAR(50),
    ADD COLUMN IF NOT EXISTS shipping_address TEXT,
    ADD COLUMN IF NOT EXISTS shipping_note TEXT;
