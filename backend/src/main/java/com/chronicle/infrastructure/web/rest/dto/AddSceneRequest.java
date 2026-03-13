package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.domain.story.EffectType;
import com.chronicle.domain.story.RequirementType;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AddSceneRequest(
        @NotBlank String title,
        String description,
        List<RequirementRequest> requirements,
        List<EffectRequest> effects,
        boolean repeatable,
        Integer priority,
        List<String> tags,
        List<String> involvedCharacters
) {
    public record RequirementRequest(String factKey, RequirementType type, String expectedValue) {}
    public record EffectRequest(EffectType type, String factKey, String factValue) {}
}
