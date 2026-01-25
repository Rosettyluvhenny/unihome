SET search_path TO unihome;

CREATE TABLE IF NOT EXISTS furniture_reviews (
    review_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    furniture_id UUID NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_review_furniture FOREIGN KEY (furniture_id) REFERENCES furniture(furniture_id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_review_user_furniture UNIQUE (furniture_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_furniture ON furniture_reviews(furniture_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user ON furniture_reviews(user_id);
