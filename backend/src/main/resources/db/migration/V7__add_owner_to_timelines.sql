ALTER TABLE timelines
    ADD COLUMN owner_id UUID REFERENCES users(id) ON DELETE CASCADE;

CREATE INDEX idx_timelines_owner_id ON timelines(owner_id);
