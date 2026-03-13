package com.chronicle.application.story.getavailablescenes;

import com.chronicle.domain.story.ContradictionSeverity;
import com.chronicle.domain.story.EffectType;
import com.chronicle.domain.story.RequirementType;

import java.util.List;

public record AvailableSceneView(
        String id,
        String title,
        String description,
        boolean repeatable,
        List<RequirementView> requirements,
        List<EffectView> effects,
        List<NarrativeContradictionView> potentialContradictions
) {
    public record RequirementView(String factKey, RequirementType type, String expectedValue) {}
    public record EffectView(EffectType type, String factKey, String factValue) {}
    public record NarrativeContradictionView(String sceneId, String factKey, String message, ContradictionSeverity severity) {}
}
