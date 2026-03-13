package com.chronicle.infrastructure.persistence.jpa;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "scene_requirements")
public class SceneRequirementEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private SceneEntity scene;

    @Column(nullable = false, length = 255)
    private String factKey;

    @Column(nullable = false, length = 50)
    private String requirementType;

    @Column(length = 500)
    private String expectedValue;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public SceneEntity getScene() { return scene; }
    public void setScene(SceneEntity scene) { this.scene = scene; }
    public String getFactKey() { return factKey; }
    public void setFactKey(String factKey) { this.factKey = factKey; }
    public String getRequirementType() { return requirementType; }
    public void setRequirementType(String requirementType) { this.requirementType = requirementType; }
    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }
}
