package com.chronicle.infrastructure.persistence.jpa;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scenes")
public class SceneEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private StoryEntity story;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean repeatable;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private int priority = 50;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "involved_characters", columnDefinition = "TEXT")
    private String involvedCharacters;

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SceneRequirementEntity> requirements = new ArrayList<>();

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SceneEffectEntity> effects = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public StoryEntity getStory() { return story; }
    public void setStory(StoryEntity story) { this.story = story; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isRepeatable() { return repeatable; }
    public void setRepeatable(boolean repeatable) { this.repeatable = repeatable; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public List<SceneRequirementEntity> getRequirements() { return requirements; }
    public void setRequirements(List<SceneRequirementEntity> requirements) { this.requirements = requirements; }
    public List<SceneEffectEntity> getEffects() { return effects; }
    public void setEffects(List<SceneEffectEntity> effects) { this.effects = effects; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getInvolvedCharacters() { return involvedCharacters; }
    public void setInvolvedCharacters(String involvedCharacters) { this.involvedCharacters = involvedCharacters; }
}
