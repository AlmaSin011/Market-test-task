CREATE TABLE orders
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id VARCHAR(255) NOT NULL,
    symbol     VARCHAR(255) NOT NULL,
    side       VARCHAR(10)  NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity   INTEGER      NOT NULL CHECK (quantity > 0),
    status     VARCHAR(20)  NOT NULL CHECK (status IN ('NEW')),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE executions
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    price       NUMERIC(18, 6) NOT NULL,
    executed_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);