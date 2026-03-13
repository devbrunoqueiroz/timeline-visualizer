package com.chronicle.domain.story;

public record NarrativeContradiction(
        SceneId sceneId,
        String factKey,
        String message,
        ContradictionSeverity severity
) {
}
