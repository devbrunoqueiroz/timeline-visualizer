package com.chronicle.domain.story;

import com.chronicle.domain.shared.AggregateRoot;
import com.chronicle.domain.shared.DomainException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorySession extends AggregateRoot<SessionId> {

    private final SessionId id;
    private final StoryId storyId;
    private final String name;
    private WorldState worldState;
    private final List<SceneId> appliedSceneIds;
    private final Instant createdAt;
    private Instant updatedAt;

    private StorySession(SessionId id, StoryId storyId, String name,
                         WorldState worldState, List<SceneId> appliedSceneIds,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.storyId = storyId;
        this.name = name;
        this.worldState = worldState;
        this.appliedSceneIds = new ArrayList<>(appliedSceneIds);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static StorySession create(StoryId storyId, String name) {
        var now = Instant.now();
        return new StorySession(SessionId.generate(), storyId, name,
                WorldState.empty(), List.of(), now, now);
    }

    public static StorySession reconstitute(SessionId id, StoryId storyId, String name,
                                            WorldState worldState, List<SceneId> appliedSceneIds,
                                            Instant createdAt, Instant updatedAt) {
        return new StorySession(id, storyId, name, worldState,
                appliedSceneIds != null ? appliedSceneIds : List.of(),
                createdAt, updatedAt);
    }

    public void applyScene(Scene scene) {
        if (!scene.isRepeatable() && appliedSceneIds.contains(scene.getId())) {
            throw new DomainException("Scene '" + scene.getTitle() + "' is not repeatable and has already been applied");
        }
        if (!scene.isAvailable(worldState)) {
            throw new DomainException("Scene '" + scene.getTitle() + "' requirements are not satisfied by current world state");
        }
        this.worldState = scene.applyTo(worldState);
        this.appliedSceneIds.add(scene.getId());
        this.updatedAt = Instant.now();
    }

    @Override
    public SessionId getId() { return id; }
    public StoryId getStoryId() { return storyId; }
    public String getName() { return name; }
    public WorldState getWorldState() { return worldState; }
    public List<SceneId> getAppliedSceneIds() { return Collections.unmodifiableList(appliedSceneIds); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
