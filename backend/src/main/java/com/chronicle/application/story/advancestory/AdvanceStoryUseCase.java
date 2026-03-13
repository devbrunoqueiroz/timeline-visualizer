package com.chronicle.application.story.advancestory;

import com.chronicle.domain.story.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdvanceStoryUseCase {

    private final StorySessionRepository sessionRepository;
    private final StoryRepository storyRepository;
    private final StoryEngine storyEngine;

    public AdvanceStoryUseCase(StorySessionRepository sessionRepository,
                                StoryRepository storyRepository,
                                StoryEngine storyEngine) {
        this.sessionRepository = sessionRepository;
        this.storyRepository = storyRepository;
        this.storyEngine = storyEngine;
    }

    public AdvanceStoryResult execute(AdvanceStoryCommand command) {
        var sessionId = SessionId.of(command.sessionId());
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new StorySessionNotFoundException(sessionId));
        var story = storyRepository.findById(session.getStoryId())
                .orElseThrow(() -> new StoryNotFoundException(session.getStoryId()));

        var result = storyEngine.advanceStory(story, session, command.strategy());

        if (result.success()) {
            session.applyScene(result.selectedScene());
            sessionRepository.save(session);
        }

        var conflicts = result.conflicts().stream()
                .map(c -> new AdvanceStoryResult.ConflictDto(
                        c.type().name(),
                        c.description(),
                        c.conflictingScene() != null ? c.conflictingScene().value().toString() : null))
                .toList();

        return new AdvanceStoryResult(
                result.success(),
                result.selectedScene() != null ? result.selectedScene().getId().value().toString() : null,
                result.selectedScene() != null ? result.selectedScene().getTitle() : null,
                result.success() ? result.newWorldState().getFacts() : null,
                conflicts,
                result.message()
        );
    }
}
