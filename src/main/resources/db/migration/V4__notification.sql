CREATE TABLE notification (
	id          VARCHAR(36) PRIMARY KEY,
	user_id     VARCHAR(36) NOT NULL,
	type        VARCHAR(50) NOT NULL,        -- VERIFY_EMAIL, ORDER_CONFIRMED, etc.
	title       VARCHAR(255) NOT NULL,
	payload     JSONB,                        -- reference + action
	channel     VARCHAR(30) NOT NULL DEFAULT 'IN_APP',
	created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	updated_at 	TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	read    boolean default false
);

-- index
CREATE INDEX idx_notification_user_created
	ON notification (user_id, created_at DESC);

-- index cho JSONB
CREATE INDEX idx_notification_payload_gin
	ON notification USING GIN (payload);

CREATE TRIGGER trg_verify_notification_at
	BEFORE UPDATE ON notification
	FOR EACH ROW
	EXECUTE FUNCTION set_updated_at();

