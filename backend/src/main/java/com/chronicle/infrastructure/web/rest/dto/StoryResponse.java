package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.application.story.getstory.StoryView;
import com.chronicle.domain.story.EffectType;
import com.chronicle.domain.story.RequirementType;

import java.time.Instant;
import java.util.List;

public record StoryResponse(
        String id,
        String title,
        String description,
        Instant createdAt,
        List<SceneResponse> scenes
) {
    public record SceneResponse(
            String id,
            String title,
            String description,
            boolean repeatable,
            int displayOrder,
            List<RequirementResponse> requirements,
            List<EffectResponse> effects,
            int priority,
            List<String> tags,
            List<String> involvedCharacters
    ) {}

    public record RequirementResponse(String factKey, RequirementType type, String expectedValue) {}
    public record EffectResponse(EffectType type, String factKey, String factValue) {}

    public static StoryResponse from(StoryView view) {
        return new StoryResponse(
                view.id(),
                view.title(),
                view.description(),
                view.createdAt(),
                view.scenes().stream()
                        .map(s -> new SceneResponse(
                                s.id(), s.title(), s.description(), s.repeatable(), s.displayOrder(),
                                s.requirements().stream()
                                        .map(r -> new RequirementResponse(r.factKey(), r.type(), r.expectedValue()))
                                        .toList(),
                                s.effects().stream()
                                        .map(e -> new EffectResponse(e.type(), e.factKey(), e.factValue()))
                                        .toList(),
                                s.priority(),
                                s.tags(),
                                s.involvedCharacters()
                        ))
                        .toList()
        );
    }
}
