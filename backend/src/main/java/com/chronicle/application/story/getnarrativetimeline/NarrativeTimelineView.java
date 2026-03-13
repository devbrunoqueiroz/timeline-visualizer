package com.chronicle.application.story.getnarrativetimeline;

import java.util.List;

public record NarrativeTimelineView(
    String sessionId,
    String sessionName,
    List<AppliedSceneView> appliedScenes
) {
    public record AppliedSceneView(String sceneId, String title, String description, int position) {}
}
