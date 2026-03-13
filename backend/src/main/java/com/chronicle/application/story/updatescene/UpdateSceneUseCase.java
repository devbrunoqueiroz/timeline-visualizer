package com.chronicle.application.story.updatescene;

import com.chronicle.domain.story.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UpdateSceneUseCase {

    private final StoryRepository storyRepository;

    public UpdateSceneUseCase(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    public void execute(UpdateSceneCommand command) {
        var storyId = StoryId.of(command.storyId());
        var story = storyRepository.findById(storyId)
                .orElseThrow(() -> new StoryNotFoundException(storyId));

        var sceneId = SceneId.of(command.sceneId());
        var scene = story.findScene(sceneId);

        var requirements = command.requirements() == null ? List.<Requirement>of() :
                command.requirements().stream()
                        .map(r -> new Requirement(r.factKey(), r.type(), r.expectedValue()))
                        .toList();

        var effects = command.effects() == null ? List.<Effect>of() :
                command.effects().stream()
                        .map(e -> new Effect(e.type(), e.factKey(), e.factValue()))
                        .toList();

        var priority = NarrativePriority.of(command.priority() != null ? command.priority() : 50);
        var tags = command.tags() != null ? command.tags() : List.<String>of();
        var involvedCharacters = command.involvedCharacters() != null ? command.involvedCharacters() : List.<String>of();

        scene.update(command.title(), command.description(), requirements, effects, command.repeatable(),
                priority, tags, involvedCharacters);
        storyRepository.save(story);
    }
}
