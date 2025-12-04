CREATE TABLE t_orders
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE t_order_line_items
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    sku_code  VARCHAR(255),
    price    DECIMAL(19, 2),
    quantity INT,
    order_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES t_orders(id)
);