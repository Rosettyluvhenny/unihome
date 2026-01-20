-- Create ChatRoom table
CREATE TABLE chat_room
(
	id         varchar(36) PRIMARY KEY,
	type       VARCHAR(10) NOT NULL CHECK (type IN ('PRIVATE', 'BOT')),
	user_a_id  varchar(36) NOT NULL,
	user_b_id  varchar(36),
	bot_type   VARCHAR(255),
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT chk_private_bot CHECK (
		(type = 'PRIVATE' AND user_b_id IS NOT NULL) OR
		(type = 'BOT' AND bot_type IS NOT NULL)
		),
	CONSTRAINT fk_chat_room_user_a FOREIGN KEY (user_a_id) REFERENCES users (id),
	CONSTRAINT fk_chat_room_user_b FOREIGN KEY (user_b_id) REFERENCES users (id)
);

-- Create ChatMessage table
CREATE TABLE chat_message
(
	id          varchar(36) PRIMARY KEY,
	room_id     varchar(36) NOT NULL REFERENCES chat_room (id),
	sender_type VARCHAR(10) NOT NULL CHECK (sender_type IN ('USER', 'BOT')),
	sender_id   varchar(36),
	content     TEXT        NOT NULL,
	created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	read        boolean   default false
);
