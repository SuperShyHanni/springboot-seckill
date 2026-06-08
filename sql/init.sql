CREATE TABLE goods (
    id BIGINT AUTO_INCREMENT,
    goods_name VARCHAR(100) NOT NULL,
    goods_price DECIMAL(10,2) NOT NULL,
    stock_count INT NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO goods (goods_name,goods_price,stock_count)
VALUES ('iPhone 15',5999.00,100);

INSERT INTO goods (goods_name,goods_price,stock_count)
VALUES ('Cola',3.00,10000);

INSERT INTO goods (goods_name,goods_price,stock_count)
VALUES ('PlayStation5',2999.00,200);

CREATE TABLE users(
    id BIGINT AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(32) NOT NULL,
    salt VARCHAR(16) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE seckill_order (
    id BIGINT AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    order_time DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_goods (user_id,goods_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (goods_id) REFERENCES goods(id)
)