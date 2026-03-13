package com.chronicle.domain.story;

import com.chronicle.domain.shared.AggregateRoot;
import com.chronicle.domain.shared.DomainException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Story extends AggregateRoot<StoryId> {

    private final StoryId id;
    private String title;
    private String description;
    private final List<Scene> scenes;
    private final Instant createdAt;
    private Instant updatedAt;

    private Story(StoryId id, String title, String description, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.scenes = new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Story create(String title, String description) {
        validateTitle(title);
        var now = Instant.now();
        return new Story(StoryId.generate(), title, description, now, now);
    }

    public static Story reconstitute(StoryId id, String title, String description,
                                     List<Scene> scenes, Instant createdAt, Instant updatedAt) {
        var story = new Story(id, title, description, createdAt, updatedAt);
        if (scenes != null) {
            story.scenes.addAll(scenes);
        }
        return story;
    }

    public Scene addScene(String title, String description, List<Requirement> requirements,
                          List<Effect> effects, boolean repeatable) {
        int order = scenes.size();
        var scene = Scene.create(title, description, requirements, effects, repeatable, order);
        scenes.add(scene);
        updatedAt = Instant.now();
        return scene;
    }

    public Scene addScene(String title, String description, List<Requirement> requirements,
                          List<Effect> effects, boolean repeatable,
                          NarrativePriority priority, List<String> tags, List<String> involvedCharacters) {
        int order = scenes.size();
        var scene = Scene.create(title, description, requirements, effects, repeatable, order, priority, tags, involvedCharacters);
        scenes.add(scene);
        updatedAt = Instant.now();
        return scene;
    }

    public void removeScene(SceneId sceneId) {
        var scene = findScene(sceneId);
        scenes.remove(scene);
        reorderScenes();
        updatedAt = Instant.now();
    }

    public Scene findScene(SceneId sceneId) {
        return scenes.stream()
                .filter(s -> s.getId().equals(sceneId))
                .findFirst()
                .orElseThrow(() -> new SceneNotFoundException(sceneId));
    }

    public void update(String title, String description) {
        validateTitle(title);
        this.title = title;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    private void reorderScenes() {
        for (int i = 0; i < scenes.size(); i++) {
            scenes.get(i).setDisplayOrder(i);
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainException("Story title cannot be blank");
        }
    }

    @Override
    public StoryId getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Scene> getScenes() { return Collections.unmodifiableList(scenes); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
