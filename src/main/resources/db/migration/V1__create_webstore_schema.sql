CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(32) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE product (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255),
    stock_quantity INT NOT NULL,
    CONSTRAINT uk_product_name UNIQUE (name),
    CONSTRAINT ck_product_price_nonnegative CHECK (price >= 0),
    CONSTRAINT ck_product_stock_nonnegative CHECK (stock_quantity >= 0)
);

CREATE TABLE tax_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    continent VARCHAR(80) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL,
    shipping_cost DECIMAL(10, 2) NOT NULL,
    CONSTRAINT uk_tax_rates_continent UNIQUE (continent),
    CONSTRAINT ck_tax_rate_nonnegative CHECK (tax_rate >= 0),
    CONSTRAINT ck_shipping_cost_nonnegative CHECK (shipping_cost >= 0)
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(32) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    status VARCHAR(32) NOT NULL,
    continent VARCHAR(80) NOT NULL,
    tax_rate DECIMAL(5, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    shipping_cost DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL,
    final_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT uk_orders_order_number UNIQUE (order_number),
    CONSTRAINT ck_order_subtotal_nonnegative CHECK (subtotal >= 0),
    CONSTRAINT ck_order_shipping_nonnegative CHECK (shipping_cost >= 0),
    CONSTRAINT ck_order_tax_nonnegative CHECK (tax_amount >= 0),
    CONSTRAINT ck_order_final_nonnegative CHECK (final_price >= 0)
);

CREATE TABLE order_lines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255),
    CONSTRAINT fk_order_lines_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT ck_order_line_price_nonnegative CHECK (unit_price >= 0),
    CONSTRAINT ck_order_line_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_order_line_total_nonnegative CHECK (line_total >= 0)
);

CREATE INDEX idx_order_lines_order_id ON order_lines (order_id);
CREATE INDEX idx_order_lines_product_id ON order_lines (product_id);
