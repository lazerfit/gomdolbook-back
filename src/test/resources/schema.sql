CREATE TABLE book
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    reading_log   VARCHAR(255) NULL,
    author        VARCHAR(255) NULL,
    category_name VARCHAR(255) NULL,
    cover         VARCHAR(255) NULL,
    description   VARCHAR(255) NULL,
    isbn13        VARCHAR(255) NULL,
    pub_date      VARCHAR(255) NULL,
    publisher     VARCHAR(255) NULL,
    title         VARCHAR(255) NULL
);


