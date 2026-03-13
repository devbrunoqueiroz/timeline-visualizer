package com.chronicle.application.story.getstory;

import com.chronicle.domain.story.EffectType;
import com.chronicle.domain.story.RequirementType;
import com.chronicle.domain.story.Story;

import java.time.Instant;
import java.util.List;

public record StoryView(
        String id,
        String title,
        String description,
        Instant createdAt,
        List<SceneView> scenes
) {
    public record SceneView(
            String id,
            String title,
            String description,
            boolean repeatable,
            int displayOrder,
            List<RequirementView> requirements,
            List<EffectView> effects,
            int priority,
            List<String> tags,
            List<String> involvedCharacters
    ) {}

    public record RequirementView(String factKey, RequirementType type, String expectedValue) {}

    public record EffectView(EffectType type, String factKey, String factValue) {}

    public static StoryView from(Story story) {
        return new StoryView(
                story.getId().toString(),
                story.getTitle(),
                story.getDescription(),
                story.getCreatedAt(),
                story.getScenes().stream()
                        .map(s -> new SceneView(
                                s.getId().toString(),
                                s.getTitle(),
                                s.getDescription(),
                                s.isRepeatable(),
                                s.getDisplayOrder(),
                                s.getRequirements().stream()
                                        .map(r -> new RequirementView(r.factKey(), r.type(), r.expectedValue()))
                                        .toList(),
                                s.getEffects().stream()
                                        .map(e -> new EffectView(e.type(), e.factKey(), e.factValue()))
                                        .toList(),
                                s.getPriority().value(),
                                s.getTags(),
                                s.getInvolvedCharacters()
                        ))
                        .toList()
        );
    }
}
