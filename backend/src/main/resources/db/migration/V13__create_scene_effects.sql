CREATE TABLE scene_effects (
    id UUID PRIMARY KEY,
    scene_id UUID NOT NULL REFERENCES scenes(id) ON DELETE CASCADE,
    effect_type VARCHAR(50) NOT NULL,
    fact_key VARCHAR(255) NOT NULL,
    fact_value VARCHAR(500)
);

CREATE INDEX idx_scene_effects_scene_id ON scene_effects(scene_id);
