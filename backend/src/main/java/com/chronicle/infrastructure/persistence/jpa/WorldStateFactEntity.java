package com.chronicle.infrastructure.persistence.jpa;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "world_state_facts")
public class WorldStateFactEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private StorySessionEntity session;

    @Column(nullable = false, length = 255)
    private String factKey;

    @Column(length = 500)
    private String factValue;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public StorySessionEntity getSession() { return session; }
    public void setSession(StorySessionEntity session) { this.session = session; }
    public String getFactKey() { return factKey; }
    public void setFactKey(String factKey) { this.factKey = factKey; }
    public String getFactValue() { return factValue; }
    public void setFactValue(String factValue) { this.factValue = factValue; }
}
