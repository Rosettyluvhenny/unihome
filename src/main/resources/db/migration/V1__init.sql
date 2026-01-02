CREATE SCHEMA IF NOT EXISTS unihome;
SET search_path TO unihome;

CREATE TABLE role (
  name VARCHAR(50) PRIMARY KEY,
  description VARCHAR(255) NULL
);

INSERT INTO role (name, description) VALUES
  ('ADMIN', 'System administrator'),
  ('CUSTOMER', 'Customer'),
  ('SHIPPER', 'Shipper'),
  ('STAFF', 'Staff');

CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'UNACTIVE',
  role_name VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	address VARCHAR(255) ,
  CONSTRAINT fk_user_role FOREIGN KEY (role_name) REFERENCES role(name)
);
