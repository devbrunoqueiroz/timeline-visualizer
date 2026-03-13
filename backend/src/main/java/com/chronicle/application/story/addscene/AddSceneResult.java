package com.chronicle.application.story.addscene;

import com.chronicle.domain.story.EffectType;
import com.chronicle.domain.story.RequirementType;
import com.chronicle.domain.story.Scene;

import java.util.List;

public record AddSceneResult(
        String id,
        String title,
        String description,
        int displayOrder,
        List<RequirementDto> requirements,
        List<EffectDto> effects,
        boolean repeatable
) {
    public record RequirementDto(String factKey, RequirementType type, String expectedValue) {}
    public record EffectDto(EffectType type, String factKey, String factValue) {}

    public static AddSceneResult from(Scene scene) {
        return new AddSceneResult(
                scene.getId().toString(),
                scene.getTitle(),
                scene.getDescription(),
                scene.getDisplayOrder(),
                scene.getRequirements().stream()
                        .map(r -> new RequirementDto(r.factKey(), r.type(), r.expectedValue()))
                        .toList(),
                scene.getEffects().stream()
                        .map(e -> new EffectDto(e.type(), e.factKey(), e.factValue()))
                        .toList(),
                scene.isRepeatable()
        );
    }
}
