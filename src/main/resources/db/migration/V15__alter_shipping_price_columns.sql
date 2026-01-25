SET search_path TO unihome;

ALTER TABLE shipping_price_distance
    ALTER COLUMN price TYPE DECIMAL(12, 2) USING price::DECIMAL(12, 2),
    ALTER COLUMN price_per_km TYPE DECIMAL(12, 2) USING price_per_km::DECIMAL(12, 2);
