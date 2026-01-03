-- Create invalidated_tokens table
CREATE TABLE IF NOT EXISTS invalidated_tokens (
  jwt_id VARCHAR(255) PRIMARY KEY NOT NULL,
  invalidated_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invalidated_tokens_expires_at ON invalidated_tokens(expires_at);

