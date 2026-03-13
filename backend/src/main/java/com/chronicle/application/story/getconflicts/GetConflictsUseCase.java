package com.chronicle.application.story.getconflicts;

import com.chronicle.domain.story.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetConflictsUseCase {

    private final StorySessionRepository sessionRepository;
    private final StoryRepository storyRepository;

    public GetConflictsUseCase(StorySessionRepository sessionRepository, StoryRepository storyRepository) {
        this.sessionRepository = sessionRepository;
        this.storyRepository = storyRepository;
    }

    public List<ConflictView> execute(GetConflictsQuery query) {
        var sessionId = SessionId.of(query.sessionId());
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new StorySessionNotFoundException(sessionId));
        var story = storyRepository.findById(session.getStoryId())
                .orElseThrow(() -> new StoryNotFoundException(session.getStoryId()));
        var scene = story.findScene(SceneId.of(query.sceneId()));
        var validator = NarrativeValidator.withDefaultRules();
        var conflicts = validator.validate(scene, session.getWorldState(), session.getAppliedSceneIds());
        return conflicts.stream()
                .map(c -> new ConflictView(
                        c.type().name(),
                        c.description(),
                        c.conflictingScene() != null ? c.conflictingScene().value().toString() : null))
                .toList();
    }
}
