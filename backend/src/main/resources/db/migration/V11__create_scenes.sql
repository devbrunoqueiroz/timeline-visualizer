CREATE TABLE scenes (
    id UUID PRIMARY KEY,
    story_id UUID NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    repeatable BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_scenes_story_id ON scenes(story_id);
