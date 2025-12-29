CREATE SCHEMA IF NOT EXISTS movie_theater;
SET search_path TO movie_theater;

CREATE TABLE role (
  name VARCHAR(50) PRIMARY KEY,
  description VARCHAR(255) NULL
);

INSERT INTO role (name, description) VALUES
  ('ADMIN', 'System administrator'),
  ('MANAGER', 'Cinema manager'),
  ('MEMBER', 'Registered member'),
  ('STAFF', 'Cinema staff');

CREATE TABLE "user" (
  id VARCHAR(36) PRIMARY KEY,
  full_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  status VARCHAR(50) NOT NULL,
  role_name VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_role FOREIGN KEY (role_name) REFERENCES role(name)
);
