CREATE TABLE timeline_connections (
    id              UUID        NOT NULL PRIMARY KEY,
    source_event_id UUID        NOT NULL REFERENCES timeline_events(id) ON DELETE CASCADE,
    target_event_id UUID        NOT NULL REFERENCES timeline_events(id) ON DELETE CASCADE,
    description     TEXT,
    connection_type VARCHAR(20) NOT NULL DEFAULT 'REFERENCE',
    created_at      TIMESTAMPTZ NOT NULL
);
