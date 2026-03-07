ALTER TABLE timeline_events
    ADD COLUMN temporal_position NUMERIC(30, 10),
    ADD COLUMN temporal_label    VARCHAR(500),
    ADD COLUMN calendar_system   VARCHAR(50);

UPDATE timeline_events
SET temporal_position = EXTRACT(EPOCH FROM occurred_at) * 1000,
    temporal_label    = TO_CHAR(occurred_at, 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
    calendar_system   = 'GREGORIAN';

ALTER TABLE timeline_events
    ALTER COLUMN temporal_position SET NOT NULL,
    ALTER COLUMN temporal_label SET NOT NULL,
    ALTER COLUMN calendar_system SET NOT NULL,
    DROP COLUMN occurred_at;
