--liquibase formatted sql

--changeset rita:1

CREATE TABLE payments
(
    id         INTEGER AUTO_INCREMENT PRIMARY KEY,
    debtor_iban VARCHAR(34) NOT NULL,
    amount     DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);