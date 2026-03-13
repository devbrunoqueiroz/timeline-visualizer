package com.chronicle.application.story.addscene;

import com.chronicle.domain.story.EffectType;
import com.chronicle.domain.story.RequirementType;

import java.util.List;

public record AddSceneCommand(
        String storyId,
        String title,
        String description,
        List<RequirementDto> requirements,
        List<EffectDto> effects,
        boolean repeatable,
        Integer priority,
        List<String> tags,
        List<String> involvedCharacters
) {
    public record RequirementDto(String factKey, RequirementType type, String expectedValue) {}
    public record EffectDto(EffectType type, String factKey, String factValue) {}
}
