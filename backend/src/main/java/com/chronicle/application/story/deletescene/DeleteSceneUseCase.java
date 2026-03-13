package com.chronicle.application.story.deletescene;

import com.chronicle.domain.story.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteSceneUseCase {

    private final StoryRepository storyRepository;

    public DeleteSceneUseCase(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    public void execute(String storyId, String sceneId) {
        var sid = StoryId.of(storyId);
        var story = storyRepository.findById(sid)
                .orElseThrow(() -> new StoryNotFoundException(sid));
        story.removeScene(SceneId.of(sceneId));
        storyRepository.save(story);
    }
}
