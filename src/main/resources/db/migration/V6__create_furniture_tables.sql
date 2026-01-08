-- Create categories table
CREATE TABLE categories (
    category_id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create furniture table
CREATE TABLE furniture (
    furniture_id UUID PRIMARY KEY,
    category_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    final_price DECIMAL(12, 2),
    stock INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    has_discount BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_furniture_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- Create indexes for furniture
CREATE INDEX idx_furniture_category ON furniture(category_id);
CREATE INDEX idx_furniture_status ON furniture(status);

-- Create discounts table
CREATE TABLE discounts (
    discount_id UUID PRIMARY KEY,
    value DECIMAL(5, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create index for discounts
CREATE INDEX idx_discount_time ON discounts(start_date, end_date);

-- Create furniture_discount junction table
CREATE TABLE furniture_discount (
    furniture_discount_id UUID PRIMARY KEY,
    furniture_id UUID NOT NULL,
    discount_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_furniture_discount_furniture FOREIGN KEY (furniture_id) REFERENCES furniture(furniture_id),
    CONSTRAINT fk_furniture_discount_discount FOREIGN KEY (discount_id) REFERENCES discounts(discount_id),
    CONSTRAINT uk_furniture_discount UNIQUE (furniture_id, discount_id)
);
