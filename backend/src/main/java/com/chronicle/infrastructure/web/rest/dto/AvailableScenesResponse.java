package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.story.getavailablescenes.AvailableSceneView;
import com.chronicle.domain.story.ContradictionSeverity;
import com.chronicle.domain.story.EffectType;
import com.chronicle.domain.story.RequirementType;

import java.util.List;

public record AvailableScenesResponse(List<AvailableSceneItem> scenes) {

    public record AvailableSceneItem(
            String id,
            String title,
            String description,
            boolean repeatable,
            List<RequirementItem> requirements,
            List<EffectItem> effects,
            List<ContradictionItem> potentialContradictions
    ) {}

    public record RequirementItem(String factKey, RequirementType type, String expectedValue) {}
    public record EffectItem(EffectType type, String factKey, String factValue) {}
    public record ContradictionItem(String sceneId, String factKey, String message, ContradictionSeverity severity) {}

    public static AvailableScenesResponse from(List<AvailableSceneView> views) {
        return new AvailableScenesResponse(
                views.stream()
                        .map(v -> new AvailableSceneItem(
                                v.id(), v.title(), v.description(), v.repeatable(),
                                v.requirements().stream()
                                        .map(r -> new RequirementItem(r.factKey(), r.type(), r.expectedValue()))
                                        .toList(),
                                v.effects().stream()
                                        .map(e -> new EffectItem(e.type(), e.factKey(), e.factValue()))
                                        .toList(),
                                v.potentialContradictions().stream()
                                        .map(c -> new ContradictionItem(c.sceneId(), c.factKey(), c.message(), c.severity()))
                                        .toList()
                        ))
                        .toList()
        );
    }
}
