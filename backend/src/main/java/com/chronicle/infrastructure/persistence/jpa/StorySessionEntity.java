package com.chronicle.infrastructure.persistence.jpa;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "story_sessions")
public class StorySessionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID storyId;

    @Column(length = 300)
    private String name;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorldStateFactEntity> worldStateFacts = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("appliedAt ASC")
    private List<SessionAppliedSceneEntity> appliedScenes = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getStoryId() { return storyId; }
    public void setStoryId(UUID storyId) { this.storyId = storyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<WorldStateFactEntity> getWorldStateFacts() { return worldStateFacts; }
    public void setWorldStateFacts(List<WorldStateFactEntity> worldStateFacts) { this.worldStateFacts = worldStateFacts; }
    public List<SessionAppliedSceneEntity> getAppliedScenes() { return appliedScenes; }
    public void setAppliedScenes(List<SessionAppliedSceneEntity> appliedScenes) { this.appliedScenes = appliedScenes; }
}
