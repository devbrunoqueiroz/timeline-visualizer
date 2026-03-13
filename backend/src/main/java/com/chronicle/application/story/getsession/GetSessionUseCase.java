package com.chronicle.application.story.getsession;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.story.StorySessionNotFoundException;
import com.chronicle.domain.story.StorySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetSessionUseCase implements UseCase<GetSessionQuery, SessionView> {

    private final StorySessionRepository sessionRepository;

    public GetSessionUseCase(StorySessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public SessionView execute(GetSessionQuery query) {
        var session = sessionRepository.findById(query.sessionId())
                .orElseThrow(() -> new StorySessionNotFoundException(query.sessionId()));
        return SessionView.from(session);
    }
}
