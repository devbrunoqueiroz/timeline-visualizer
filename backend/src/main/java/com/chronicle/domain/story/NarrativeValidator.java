package com.chronicle.domain.story;

import java.util.ArrayList;
import java.util.List;

public class NarrativeValidator {

    private final List<NarrativeRule> rules;

    public NarrativeValidator(List<NarrativeRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public static NarrativeValidator withDefaultRules() {
        return new NarrativeValidator(List.of(
            new AlivePresenceRule(),
            new UniqueEventRule(),
            new CausalityRule()
        ));
    }

    public List<NarrativeConflict> validate(Scene scene, WorldState worldState, List<SceneId> appliedSceneIds) {
        var all = new ArrayList<NarrativeConflict>();
        for (var rule : rules) {
            all.addAll(rule.validate(scene, worldState, appliedSceneIds));
        }
        return all;
    }
}
