CREATE TABLE verify_token (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_verify_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_verify_token_user_id ON verify_token (user_id);
CREATE INDEX idx_verify_token_expires_at ON verify_token (expires_at);

-- trigger for updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- trigger updated_at for verify_token
CREATE TRIGGER trg_verify_token_updated_at
    BEFORE UPDATE ON verify_token
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_verify_user_updated_at
	BEFORE UPDATE ON users
	FOR EACH ROW
	EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_verify_refresh_at
	BEFORE UPDATE ON refresh_token
	FOR EACH ROW
	EXECUTE FUNCTION set_updated_at();


