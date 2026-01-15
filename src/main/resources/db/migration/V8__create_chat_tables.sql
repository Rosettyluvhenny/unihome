-- Create ChatRoom table
CREATE TABLE chat_room
(
	id         UUID PRIMARY KEY,
	type       VARCHAR(10) NOT NULL CHECK (type IN ('PRIVATE', 'BOT')),
	user_a_id  UUID        NOT NULL,
	user_b_id  UUID,
	bot_type   VARCHAR(255),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT chk_private_bot CHECK (
		(type = 'PRIVATE' AND user_b_id IS NOT NULL) OR
		(type = 'BOT' AND bot_type IS NOT NULL)
		)
);

-- Create ChatMessage table
CREATE TABLE chat_message
(
	id          UUID PRIMARY KEY,
	room_id     UUID        NOT NULL REFERENCES chat_room (id),
	sender_type VARCHAR(10) NOT NULL CHECK (sender_type IN ('USER', 'BOT')),
	sender_id   UUID,
	content     TEXT        NOT NULL,
	created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	read        boolean   default false
);
