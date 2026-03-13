package com.chronicle.domain.story;

public record NarrativeConflict(ConflictType type, String description, SceneId conflictingScene) {

    public static NarrativeConflict deadCharacter(SceneId scene, String characterId) {
        return new NarrativeConflict(
            ConflictType.DEAD_CHARACTER_IN_SCENE,
            "Character '" + characterId + "' is dead but appears in this scene",
            scene
        );
    }

    public static NarrativeConflict uniqueViolation(SceneId scene) {
        return new NarrativeConflict(
            ConflictType.UNIQUE_EVENT_REPEATED,
            "This non-repeatable scene has already been applied",
            scene
        );
    }

    public static NarrativeConflict contradiction(SceneId scene, String factKey, String oldVal, String newVal) {
        return new NarrativeConflict(
            ConflictType.CONTRADICTORY_FACTS,
            "Effect would change '" + factKey + "' from '" + oldVal + "' to '" + newVal + "'",
            scene
        );
    }
}
