package com.chronicle.application.story.applyscene;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.story.SceneId;
import com.chronicle.domain.story.SessionId;
import com.chronicle.domain.story.StoryNotFoundException;
import com.chronicle.domain.story.StoryRepository;
import com.chronicle.domain.story.StorySessionNotFoundException;
import com.chronicle.domain.story.StorySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplySceneUseCase implements UseCase<ApplySceneCommand, ApplySceneResult> {

    private final StorySessionRepository sessionRepository;
    private final StoryRepository storyRepository;

    public ApplySceneUseCase(StorySessionRepository sessionRepository, StoryRepository storyRepository) {
        this.sessionRepository = sessionRepository;
        this.storyRepository = storyRepository;
    }

    @Override
    public ApplySceneResult execute(ApplySceneCommand command) {
        var sessionId = SessionId.of(command.sessionId());
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new StorySessionNotFoundException(sessionId));

        var story = storyRepository.findById(session.getStoryId())
                .orElseThrow(() -> new StoryNotFoundException(session.getStoryId()));

        var sceneId = SceneId.of(command.sceneId());
        var scene = story.findScene(sceneId);

        session.applyScene(scene);
        sessionRepository.save(session);

        return new ApplySceneResult(
                session.getId().toString(),
                session.getWorldState().getFacts(),
                session.getAppliedSceneIds().stream().map(id -> id.toString()).toList(),
                scene.getTitle()
        );
    }
}
