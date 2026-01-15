CREATE TABLE IF NOT EXISTS furniture_images (
    image_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    furniture_id UUID NOT NULL REFERENCES furniture(furniture_id) ON DELETE CASCADE,
    image_url VARCHAR(1024) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_furniture_images_furniture ON furniture_images(furniture_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_furniture_images_primary ON furniture_images(furniture_id) WHERE is_primary;
