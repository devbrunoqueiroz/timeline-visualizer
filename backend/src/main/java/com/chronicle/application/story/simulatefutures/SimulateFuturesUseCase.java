package com.chronicle.application.story.simulatefutures;

import com.chronicle.domain.story.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SimulateFuturesUseCase {

    private final StorySessionRepository sessionRepository;
    private final StoryRepository storyRepository;
    private final StoryEngine storyEngine;

    public SimulateFuturesUseCase(StorySessionRepository sessionRepository,
                                   StoryRepository storyRepository,
                                   StoryEngine storyEngine) {
        this.sessionRepository = sessionRepository;
        this.storyRepository = storyRepository;
        this.storyEngine = storyEngine;
    }

    public List<SimulationView> execute(SimulateFuturesQuery query) {
        var sessionId = SessionId.of(query.sessionId());
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new StorySessionNotFoundException(sessionId));
        var story = storyRepository.findById(session.getStoryId())
                .orElseThrow(() -> new StoryNotFoundException(session.getStoryId()));

        int depth = query.maxDepth() > 0 && query.maxDepth() <= 5 ? query.maxDepth() : 3;
        var futures = storyEngine.simulateFutures(story, session, depth);

        return futures.stream()
                .map(f -> new SimulationView(
                        f.scenePath().stream().map(id -> id.value().toString()).toList(),
                        f.sceneTitles(),
                        f.finalWorldState().getFacts(),
                        f.depth()))
                .toList();
    }
}
