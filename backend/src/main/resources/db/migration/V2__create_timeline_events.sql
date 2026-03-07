CREATE TABLE timeline_events (
    id            UUID         NOT NULL PRIMARY KEY,
    timeline_id   UUID         NOT NULL REFERENCES timelines(id) ON DELETE CASCADE,
    title         VARCHAR(300) NOT NULL,
    content_text  TEXT,
    content_type  VARCHAR(20)  NOT NULL DEFAULT 'TEXT',
    occurred_at   TIMESTAMPTZ  NOT NULL,
    display_order INTEGER      NOT NULL DEFAULT 0
);
