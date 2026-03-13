package com.chronicle.domain.story;

import java.util.List;

public interface NarrativeRule {
    List<NarrativeConflict> validate(Scene scene, WorldState worldState, List<SceneId> appliedSceneIds);
}
