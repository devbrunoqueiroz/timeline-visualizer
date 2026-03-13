package com.chronicle.application.story.getavailablescenes;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.story.StoryEngine;
import com.chronicle.domain.story.StoryNotFoundException;
import com.chronicle.domain.story.StoryRepository;
import com.chronicle.domain.story.StorySessionNotFoundException;
import com.chronicle.domain.story.StorySessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetAvailableScenesUseCase implements UseCase<GetAvailableScenesQuery, List<AvailableSceneView>> {

    private final StorySessionRepository sessionRepository;
    private final StoryRepository storyRepository;
    private final StoryEngine storyEngine;

    public GetAvailableScenesUseCase(StorySessionRepository sessionRepository,
                                     StoryRepository storyRepository,
                                     StoryEngine storyEngine) {
        this.sessionRepository = sessionRepository;
        this.storyRepository = storyRepository;
        this.storyEngine = storyEngine;
    }

    @Override
    public List<AvailableSceneView> execute(GetAvailableScenesQuery query) {
        var session = sessionRepository.findById(query.sessionId())
                .orElseThrow(() -> new StorySessionNotFoundException(query.sessionId()));
        var story = storyRepository.findById(session.getStoryId())
                .orElseThrow(() -> new StoryNotFoundException(session.getStoryId()));

        var availableScenes = storyEngine.getAvailableScenes(
                story, session.getWorldState(), session.getAppliedSceneIds());

        return availableScenes.stream()
                .map(scene -> {
                    var contradictions = storyEngine.checkConsistency(scene, session.getWorldState());
                    return new AvailableSceneView(
                            scene.getId().toString(),
                            scene.getTitle(),
                            scene.getDescription(),
                            scene.isRepeatable(),
                            scene.getRequirements().stream()
                                    .map(r -> new AvailableSceneView.RequirementView(r.factKey(), r.type(), r.expectedValue()))
                                    .toList(),
                            scene.getEffects().stream()
                                    .map(e -> new AvailableSceneView.EffectView(e.type(), e.factKey(), e.factValue()))
                                    .toList(),
                            contradictions.stream()
                                    .map(c -> new AvailableSceneView.NarrativeContradictionView(
                                            c.sceneId().toString(), c.factKey(), c.message(), c.severity()))
                                    .toList()
                    );
                })
                .toList();
    }
}
