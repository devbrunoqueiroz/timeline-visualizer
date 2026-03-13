CREATE TABLE session_applied_scenes (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES story_sessions(id) ON DELETE CASCADE,
    scene_id UUID NOT NULL,
    applied_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_session_applied_scenes_session_id ON session_applied_scenes(session_id);
