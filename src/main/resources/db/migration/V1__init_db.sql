CREATE TABLE socks
(
    id            BIGSERIAL PRIMARY KEY ,
    brand         TEXT,
    article       TEXT,
    color         TEXT,
    cotton_part   INTEGER,
    quantity      INTEGER DEFAULT 0,
    created_time  TIMESTAMP WITH TIME ZONE,
    updated_time  TIMESTAMP WITH TIME ZONE

);