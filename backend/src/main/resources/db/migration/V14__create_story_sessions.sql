CREATE TABLE story_sessions (
    id UUID PRIMARY KEY,
    story_id UUID NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    name VARCHAR(300),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_story_sessions_story_id ON story_sessions(story_id);
