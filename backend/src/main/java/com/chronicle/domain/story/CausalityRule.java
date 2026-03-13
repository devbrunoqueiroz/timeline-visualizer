package com.chronicle.domain.story;

import java.util.ArrayList;
import java.util.List;

public class CausalityRule implements NarrativeRule {

    @Override
    public List<NarrativeConflict> validate(Scene scene, WorldState worldState, List<SceneId> appliedSceneIds) {
        var conflicts = new ArrayList<NarrativeConflict>();
        for (var effect : scene.getEffects()) {
            if ((effect.type() == EffectType.SET_FACT || effect.type() == EffectType.ADD_FACT)
                    && worldState.hasFact(effect.factKey())) {
                var existing = worldState.getFactValue(effect.factKey()).orElse(null);
                var newVal = effect.factValue() != null ? effect.factValue() : "";
                if (existing != null && !existing.equals(newVal)) {
                    conflicts.add(NarrativeConflict.contradiction(scene.getId(), effect.factKey(), existing, newVal));
                }
            }
        }
        return conflicts;
    }
}
