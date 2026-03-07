-- Shipment table: links to transaction, assigned to a shipper
CREATE TABLE shipment (
    id              VARCHAR(36)     PRIMARY KEY,
    transaction_id  VARCHAR(36)     NOT NULL,
    shipper_id      VARCHAR(255)    NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    note            TEXT,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shipment_transaction FOREIGN KEY (transaction_id) REFERENCES transaction(id),
    CONSTRAINT fk_shipment_shipper     FOREIGN KEY (shipper_id)     REFERENCES users(id),
    CONSTRAINT uq_shipment_transaction UNIQUE (transaction_id)
);
