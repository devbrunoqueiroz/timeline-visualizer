package com.chronicle.application.story.getnarrativetimeline;

import com.chronicle.domain.story.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional(readOnly = true)
public class GetNarrativeTimelineUseCase {

    private final StorySessionRepository sessionRepository;
    private final StoryRepository storyRepository;

    public GetNarrativeTimelineUseCase(StorySessionRepository sessionRepository,
                                        StoryRepository storyRepository) {
        this.sessionRepository = sessionRepository;
        this.storyRepository = storyRepository;
    }

    public NarrativeTimelineView execute(String sessionId) {
        var sid = SessionId.of(sessionId);
        var session = sessionRepository.findById(sid)
                .orElseThrow(() -> new StorySessionNotFoundException(sid));
        var story = storyRepository.findById(session.getStoryId())
                .orElseThrow(() -> new StoryNotFoundException(session.getStoryId()));

        var appliedScenes = new ArrayList<NarrativeTimelineView.AppliedSceneView>();
        var sceneIds = session.getAppliedSceneIds();
        for (int i = 0; i < sceneIds.size(); i++) {
            try {
                var scene = story.findScene(sceneIds.get(i));
                appliedScenes.add(new NarrativeTimelineView.AppliedSceneView(
                        scene.getId().value().toString(),
                        scene.getTitle(),
                        scene.getDescription(),
                        i
                ));
            } catch (SceneNotFoundException ignored) {
                // Scene may have been deleted
            }
        }

        return new NarrativeTimelineView(
                session.getId().value().toString(),
                session.getName(),
                appliedScenes
        );
    }
}
