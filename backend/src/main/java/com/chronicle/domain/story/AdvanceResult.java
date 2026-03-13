package com.chronicle.domain.story;

import java.util.List;

public record AdvanceResult(
    boolean success,
    Scene selectedScene,
    WorldState newWorldState,
    List<NarrativeConflict> conflicts,
    String message
) {

    public static AdvanceResult noScenesAvailable() {
        return new AdvanceResult(false, null, null, List.of(), "No scenes available to advance the story");
    }

    public static AdvanceResult advanced(Scene scene, WorldState newState, List<NarrativeConflict> conflicts) {
        return new AdvanceResult(true, scene, newState, conflicts, "Scene applied: " + scene.getTitle());
    }
}
