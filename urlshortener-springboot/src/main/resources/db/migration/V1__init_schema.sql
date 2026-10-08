-- Flyway migrations are immutable once applied: never edit this file after
-- it has run against any database (dev, CI, prod). Need a change? Add
-- V2__something.sql instead. Flyway tracks which scripts have run in the
-- flyway_schema_history table so it never re-applies one.

CREATE TABLE IF NOT EXISTS urls (
    id         BIGSERIAL PRIMARY KEY,
    long_url   TEXT UNIQUE NOT NULL,
    short_url  TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS click_analytics (
    id         UUID PRIMARY KEY,
    short_url  TEXT NOT NULL,
    long_url   TEXT NOT NULL,
    timestamp  TIMESTAMP NOT NULL,
    ip         TEXT,
    user_agent TEXT,
    referer    TEXT
);

-- Both original lookups (long_url -> short_url and short_url -> long_url)
-- already benefit from the UNIQUE constraints above, which Postgres backs
-- with an implicit b-tree index. This index adds a fast path for the
-- analytics dashboard use case: "give me every click for this short_url".
CREATE INDEX IF NOT EXISTS idx_click_analytics_short_url ON click_analytics (short_url);
