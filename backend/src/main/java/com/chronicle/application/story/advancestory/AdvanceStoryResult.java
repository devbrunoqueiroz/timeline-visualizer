package com.chronicle.application.story.advancestory;

import java.util.List;
import java.util.Map;

public record AdvanceStoryResult(
    boolean success,
    String sceneId,
    String sceneTitle,
    Map<String, String> newWorldState,
    List<ConflictDto> conflicts,
    String message
) {
    public record ConflictDto(String type, String description, String conflictingSceneId) {}
}
