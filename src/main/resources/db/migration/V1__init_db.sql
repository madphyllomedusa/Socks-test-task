CREATE TABLE socks
(
    id          BIGSERIAL PRIMARY KEY ,
    color       TEXT,
    cotton_part INTEGER,
    quantity    INTEGER DEFAULT 0
);