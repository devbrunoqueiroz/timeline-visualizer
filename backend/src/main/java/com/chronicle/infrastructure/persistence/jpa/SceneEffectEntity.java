package com.chronicle.infrastructure.persistence.jpa;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "scene_effects")
public class SceneEffectEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private SceneEntity scene;

    @Column(nullable = false, length = 50)
    private String effectType;

    @Column(nullable = false, length = 255)
    private String factKey;

    @Column(length = 500)
    private String factValue;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public SceneEntity getScene() { return scene; }
    public void setScene(SceneEntity scene) { this.scene = scene; }
    public String getEffectType() { return effectType; }
    public void setEffectType(String effectType) { this.effectType = effectType; }
    public String getFactKey() { return factKey; }
    public void setFactKey(String factKey) { this.factKey = factKey; }
    public String getFactValue() { return factValue; }
    public void setFactValue(String factValue) { this.factValue = factValue; }
}
