package com.chronicle.domain.story;

import com.chronicle.domain.shared.DomainException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scene {

    private final SceneId id;
    private String title;
    private String description;
    private final List<Requirement> requirements;
    private final List<Effect> effects;
    private boolean repeatable;
    private int displayOrder;
    private NarrativePriority priority;
    private final List<String> tags;
    private final List<String> involvedCharacters;

    private Scene(SceneId id, String title, String description,
                  List<Requirement> requirements, List<Effect> effects,
                  boolean repeatable, int displayOrder,
                  NarrativePriority priority, List<String> tags, List<String> involvedCharacters) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.requirements = new ArrayList<>(requirements);
        this.effects = new ArrayList<>(effects);
        this.repeatable = repeatable;
        this.displayOrder = displayOrder;
        this.priority = priority;
        this.tags = new ArrayList<>(tags);
        this.involvedCharacters = new ArrayList<>(involvedCharacters);
    }

    public static Scene create(String title, String description,
                               List<Requirement> requirements, List<Effect> effects,
                               boolean repeatable, int displayOrder) {
        validateTitle(title);
        return new Scene(SceneId.generate(), title, description,
                requirements != null ? requirements : List.of(),
                effects != null ? effects : List.of(),
                repeatable, displayOrder,
                NarrativePriority.defaultPriority(), List.of(), List.of());
    }

    public static Scene create(String title, String description,
                               List<Requirement> requirements, List<Effect> effects,
                               boolean repeatable, int displayOrder,
                               NarrativePriority priority, List<String> tags, List<String> involvedCharacters) {
        validateTitle(title);
        return new Scene(SceneId.generate(), title, description,
                requirements != null ? requirements : List.of(),
                effects != null ? effects : List.of(),
                repeatable, displayOrder,
                priority != null ? priority : NarrativePriority.defaultPriority(),
                tags != null ? tags : List.of(),
                involvedCharacters != null ? involvedCharacters : List.of());
    }

    public static Scene reconstitute(SceneId id, String title, String description,
                                     List<Requirement> requirements, List<Effect> effects,
                                     boolean repeatable, int displayOrder) {
        return new Scene(id, title, description,
                requirements != null ? requirements : List.of(),
                effects != null ? effects : List.of(),
                repeatable, displayOrder,
                NarrativePriority.defaultPriority(), List.of(), List.of());
    }

    public static Scene reconstitute(SceneId id, String title, String description,
                                     List<Requirement> requirements, List<Effect> effects,
                                     boolean repeatable, int displayOrder,
                                     NarrativePriority priority, List<String> tags, List<String> involvedCharacters) {
        return new Scene(id, title, description,
                requirements != null ? requirements : List.of(),
                effects != null ? effects : List.of(),
                repeatable, displayOrder,
                priority != null ? priority : NarrativePriority.defaultPriority(),
                tags != null ? tags : List.of(),
                involvedCharacters != null ? involvedCharacters : List.of());
    }

    public boolean isAvailable(WorldState worldState) {
        return worldState.satisfiesAll(requirements);
    }

    public WorldState applyTo(WorldState worldState) {
        return worldState.applyAll(effects);
    }

    public void update(String title, String description, List<Requirement> requirements,
                       List<Effect> effects, boolean repeatable) {
        validateTitle(title);
        this.title = title;
        this.description = description;
        this.requirements.clear();
        if (requirements != null) this.requirements.addAll(requirements);
        this.effects.clear();
        if (effects != null) this.effects.addAll(effects);
        this.repeatable = repeatable;
    }

    public void update(String title, String description, List<Requirement> requirements,
                       List<Effect> effects, boolean repeatable,
                       NarrativePriority priority, List<String> tags, List<String> involvedCharacters) {
        update(title, description, requirements, effects, repeatable);
        this.priority = priority != null ? priority : NarrativePriority.defaultPriority();
        this.tags.clear();
        if (tags != null) this.tags.addAll(tags);
        this.involvedCharacters.clear();
        if (involvedCharacters != null) this.involvedCharacters.addAll(involvedCharacters);
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainException("Scene title cannot be blank");
        }
    }

    public SceneId getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Requirement> getRequirements() { return Collections.unmodifiableList(requirements); }
    public List<Effect> getEffects() { return Collections.unmodifiableList(effects); }
    public boolean isRepeatable() { return repeatable; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public NarrativePriority getPriority() { return priority; }
    public List<String> getTags() { return Collections.unmodifiableList(tags); }
    public List<String> getInvolvedCharacters() { return Collections.unmodifiableList(involvedCharacters); }
}
