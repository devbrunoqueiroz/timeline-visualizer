package com.chronicle.application.story.applyscene;

import java.util.List;
import java.util.Map;

public record ApplySceneResult(
        String sessionId,
        Map<String, String> newWorldStateFacts,
        List<String> appliedSceneIds,
        String appliedSceneTitle
) {
}
