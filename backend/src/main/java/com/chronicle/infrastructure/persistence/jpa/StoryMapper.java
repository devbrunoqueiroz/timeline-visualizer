package com.chronicle.infrastructure.persistence.jpa;

import com.chronicle.domain.story.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class StoryMapper {

    public StoryEntity toEntity(Story story) {
        var entity = new StoryEntity();
        entity.setId(story.getId().value());
        entity.setTitle(story.getTitle());
        entity.setDescription(story.getDescription());
        entity.setCreatedAt(story.getCreatedAt());
        entity.setUpdatedAt(story.getUpdatedAt());

        var sceneEntities = story.getScenes().stream()
                .map(scene -> toSceneEntity(scene, entity))
                .toList();
        entity.getScenes().clear();
        entity.getScenes().addAll(sceneEntities);
        return entity;
    }

    public Story toDomain(StoryEntity entity) {
        var scenes = entity.getScenes().stream()
                .map(this::toSceneDomain)
                .toList();
        return Story.reconstitute(
                new StoryId(entity.getId()),
                entity.getTitle(),
                entity.getDescription(),
                scenes,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private SceneEntity toSceneEntity(Scene scene, StoryEntity storyEntity) {
        var entity = new SceneEntity();
        entity.setId(scene.getId().value());
        entity.setStory(storyEntity);
        entity.setTitle(scene.getTitle());
        entity.setDescription(scene.getDescription());
        entity.setRepeatable(scene.isRepeatable());
        entity.setDisplayOrder(scene.getDisplayOrder());
        entity.setPriority(scene.getPriority().value());
        entity.setTags(scene.getTags().isEmpty() ? null : String.join(",", scene.getTags()));
        entity.setInvolvedCharacters(scene.getInvolvedCharacters().isEmpty() ? null : String.join(",", scene.getInvolvedCharacters()));

        var reqEntities = scene.getRequirements().stream()
                .map(r -> toRequirementEntity(r, entity))
                .toList();
        entity.getRequirements().clear();
        entity.getRequirements().addAll(reqEntities);

        var effEntities = scene.getEffects().stream()
                .map(e -> toEffectEntity(e, entity))
                .toList();
        entity.getEffects().clear();
        entity.getEffects().addAll(effEntities);

        return entity;
    }

    private SceneRequirementEntity toRequirementEntity(Requirement requirement, SceneEntity sceneEntity) {
        var entity = new SceneRequirementEntity();
        entity.setId(UUID.randomUUID());
        entity.setScene(sceneEntity);
        entity.setFactKey(requirement.factKey());
        entity.setRequirementType(requirement.type().name());
        entity.setExpectedValue(requirement.expectedValue());
        return entity;
    }

    private SceneEffectEntity toEffectEntity(Effect effect, SceneEntity sceneEntity) {
        var entity = new SceneEffectEntity();
        entity.setId(UUID.randomUUID());
        entity.setScene(sceneEntity);
        entity.setEffectType(effect.type().name());
        entity.setFactKey(effect.factKey());
        entity.setFactValue(effect.factValue());
        return entity;
    }

    private Scene toSceneDomain(SceneEntity entity) {
        var requirements = entity.getRequirements().stream()
                .map(r -> new Requirement(r.getFactKey(), RequirementType.valueOf(r.getRequirementType()), r.getExpectedValue()))
                .toList();
        var effects = entity.getEffects().stream()
                .map(e -> new Effect(EffectType.valueOf(e.getEffectType()), e.getFactKey(), e.getFactValue()))
                .toList();
        var priority = NarrativePriority.of(entity.getPriority());
        var tags = entity.getTags() == null || entity.getTags().isBlank()
            ? List.<String>of()
            : Arrays.asList(entity.getTags().split(","));
        var involvedCharacters = entity.getInvolvedCharacters() == null || entity.getInvolvedCharacters().isBlank()
            ? List.<String>of()
            : Arrays.asList(entity.getInvolvedCharacters().split(","));
        return Scene.reconstitute(
                new SceneId(entity.getId()),
                entity.getTitle(),
                entity.getDescription(),
                requirements,
                effects,
                entity.isRepeatable(),
                entity.getDisplayOrder(),
                priority,
                tags,
                involvedCharacters
        );
    }

    public StorySessionEntity toSessionEntity(StorySession session) {
        var entity = new StorySessionEntity();
        entity.setId(session.getId().value());
        entity.setStoryId(session.getStoryId().value());
        entity.setName(session.getName());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setUpdatedAt(session.getUpdatedAt());

        // World state facts
        var factEntities = session.getWorldState().getFacts().entrySet().stream()
                .map(entry -> {
                    var factEntity = new WorldStateFactEntity();
                    factEntity.setId(UUID.randomUUID());
                    factEntity.setSession(entity);
                    factEntity.setFactKey(entry.getKey());
                    factEntity.setFactValue(entry.getValue());
                    return factEntity;
                })
                .toList();
        entity.getWorldStateFacts().clear();
        entity.getWorldStateFacts().addAll(factEntities);

        // Applied scenes
        var appliedEntities = session.getAppliedSceneIds().stream()
                .map(sceneId -> {
                    var appliedEntity = new SessionAppliedSceneEntity();
                    appliedEntity.setId(UUID.randomUUID());
                    appliedEntity.setSession(entity);
                    appliedEntity.setSceneId(sceneId.value());
                    appliedEntity.setAppliedAt(Instant.now());
                    return appliedEntity;
                })
                .toList();
        entity.getAppliedScenes().clear();
        entity.getAppliedScenes().addAll(appliedEntities);

        return entity;
    }

    public StorySession toSessionDomain(StorySessionEntity entity) {
        var facts = new java.util.HashMap<String, String>();
        entity.getWorldStateFacts().forEach(f -> facts.put(f.getFactKey(), f.getFactValue()));
        var worldState = WorldState.of(facts);

        var appliedSceneIds = entity.getAppliedScenes().stream()
                .map(a -> new SceneId(a.getSceneId()))
                .toList();

        return StorySession.reconstitute(
                new SessionId(entity.getId()),
                new StoryId(entity.getStoryId()),
                entity.getName(),
                worldState,
                appliedSceneIds,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
