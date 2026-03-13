package com.chronicle.domain.story;

import java.util.List;

public class UniqueEventRule implements NarrativeRule {

    @Override
    public List<NarrativeConflict> validate(Scene scene, WorldState worldState, List<SceneId> appliedSceneIds) {
        if (!scene.isRepeatable() && appliedSceneIds.contains(scene.getId())) {
            return List.of(NarrativeConflict.uniqueViolation(scene.getId()));
        }
        return List.of();
    }
}
