CREATE TABLE characters (
    id UUID PRIMARY KEY,
    timeline_id UUID NOT NULL REFERENCES timelines(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    start_position NUMERIC,
    end_position NUMERIC,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE character_events (
    id UUID PRIMARY KEY,
    character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    content_text TEXT,
    content_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    temporal_position NUMERIC NOT NULL,
    temporal_label VARCHAR(500) NOT NULL,
    calendar_system VARCHAR(50) NOT NULL DEFAULT 'CUSTOM',
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_characters_timeline_id ON characters(timeline_id);
CREATE INDEX idx_character_events_character_id ON character_events(character_id);
