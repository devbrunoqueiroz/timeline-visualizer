package com.chronicle.infrastructure.persistence.jpa;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "session_applied_scenes")
public class SessionAppliedSceneEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private StorySessionEntity session;

    @Column(nullable = false)
    private UUID sceneId;

    @Column(nullable = false)
    private Instant appliedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public StorySessionEntity getSession() { return session; }
    public void setSession(StorySessionEntity session) { this.session = session; }
    public UUID getSceneId() { return sceneId; }
    public void setSceneId(UUID sceneId) { this.sceneId = sceneId; }
    public Instant getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Instant appliedAt) { this.appliedAt = appliedAt; }
}
