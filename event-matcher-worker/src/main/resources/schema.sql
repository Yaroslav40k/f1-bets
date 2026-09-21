CREATE TABLE IF NOT EXISTS bets (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    event_id        UUID NOT NULL,
    event_market_id UUID NOT NULL,
    event_winner_id UUID,
    stake           DECIMAL(19, 2) NOT NULL,
    status          VARCHAR(16) NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bets_event_id_status ON bets (event_id, status);

CREATE TABLE IF NOT EXISTS outbox_event (
    id            UUID PRIMARY KEY,
    aggregate_id  UUID,
    topic         VARCHAR(255) NOT NULL,
    partition_key VARCHAR(255),
    payload       CLOB NOT NULL,
    status        VARCHAR(16) NOT NULL,
    retry_count   INT NOT NULL DEFAULT 0,
    published_at  TIMESTAMP,
    last_error    CLOB,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outbox_event_status_retry ON outbox_event (status, retry_count);
