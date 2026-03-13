package com.chronicle.application.story.createstory;

import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.story.Story;
import com.chronicle.domain.story.StoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateStoryUseCase implements UseCase<CreateStoryCommand, CreateStoryResult> {

    private final StoryRepository storyRepository;

    public CreateStoryUseCase(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }

    @Override
    public CreateStoryResult execute(CreateStoryCommand command) {
        var story = Story.create(command.title(), command.description());
        storyRepository.save(story);
        return CreateStoryResult.from(story);
    }
}
