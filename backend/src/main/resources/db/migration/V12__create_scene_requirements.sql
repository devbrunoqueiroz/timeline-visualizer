CREATE TABLE scene_requirements (
    id UUID PRIMARY KEY,
    scene_id UUID NOT NULL REFERENCES scenes(id) ON DELETE CASCADE,
    fact_key VARCHAR(255) NOT NULL,
    requirement_type VARCHAR(50) NOT NULL,
    expected_value VARCHAR(500)
);

CREATE INDEX idx_scene_requirements_scene_id ON scene_requirements(scene_id);
