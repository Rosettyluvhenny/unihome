CREATE TABLE payment
(
	id         VARCHAR(50) PRIMARY KEY,
	name       VARCHAR(100)                NOT NULL,
	is_active  BOOLEAN                     NOT NULL DEFAULT TRUE,
	created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transaction
(
	id            VARCHAR(36) PRIMARY KEY,
	order_id      UUID,
	user_boost_id varchar(36),
	payment_id    VARCHAR(50)                 NOT NULL,

	status        VARCHAR(30)                 NOT NULL DEFAULT 'PENDING',
	url           TEXT,
	paid_at       TIMESTAMP WITHOUT TIME ZONE,

	created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

	expired_at    TIMESTAMP                            DEFAULT NULL,

	pay_os_code   varchar(255),
	CONSTRAINT fk_transaction_payment
		FOREIGN KEY (payment_id)
			REFERENCES payment (id),

	CONSTRAINT fk_transaction_order
		FOREIGN KEY (order_id)
			REFERENCES orders (order_id),

	CONSTRAINT fk_transaction_user_boost
		FOREIGN KEY (user_boost_id)
			REFERENCES user_boost (id)
);
