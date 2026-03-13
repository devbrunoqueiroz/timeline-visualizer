package com.chronicle.application.story.createsession;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.story.StoryId;
import com.chronicle.domain.story.StoryNotFoundException;
import com.chronicle.domain.story.StoryRepository;
import com.chronicle.domain.story.StorySession;
import com.chronicle.domain.story.StorySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateSessionUseCase implements UseCase<CreateSessionCommand, CreateSessionResult> {

    private final StoryRepository storyRepository;
    private final StorySessionRepository sessionRepository;

    public CreateSessionUseCase(StoryRepository storyRepository, StorySessionRepository sessionRepository) {
        this.storyRepository = storyRepository;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public CreateSessionResult execute(CreateSessionCommand command) {
        var storyId = StoryId.of(command.storyId());
        if (!storyRepository.existsById(storyId)) {
            throw new StoryNotFoundException(storyId);
        }
        var session = StorySession.create(storyId, command.name());
        sessionRepository.save(session);
        return CreateSessionResult.from(session);
    }
}
