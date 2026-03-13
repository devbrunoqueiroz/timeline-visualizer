package com.chronicle.domain.story;

import java.util.ArrayList;
import java.util.List;

public class AlivePresenceRule implements NarrativeRule {

    @Override
    public List<NarrativeConflict> validate(Scene scene, WorldState worldState, List<SceneId> appliedSceneIds) {
        var conflicts = new ArrayList<NarrativeConflict>();
        for (var characterId : scene.getInvolvedCharacters()) {
            if (worldState.hasFact(characterId + ".dead")) {
                conflicts.add(NarrativeConflict.deadCharacter(scene.getId(), characterId));
            }
        }
        return conflicts;
    }
}
