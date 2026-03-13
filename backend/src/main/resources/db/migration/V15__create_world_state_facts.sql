CREATE TABLE world_state_facts (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES story_sessions(id) ON DELETE CASCADE,
    fact_key VARCHAR(255) NOT NULL,
    fact_value VARCHAR(500)
);

CREATE INDEX idx_world_state_facts_session_id ON world_state_facts(session_id);
