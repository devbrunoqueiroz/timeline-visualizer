package com.chronicle.infrastructure.persistence.jpa;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "timeline_events")
public class TimelineEventEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timeline_id", nullable = false)
    private TimelineEntity timeline;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contentText;

    @Column(length = 20)
    private String contentType;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal temporalPosition;

    @Column(nullable = false, length = 500)
    private String temporalLabel;

    @Column(nullable = false, length = 50)
    private String calendarSystem;

    @Column(nullable = false)
    private Integer displayOrder;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TimelineEntity getTimeline() { return timeline; }
    public void setTimeline(TimelineEntity timeline) { this.timeline = timeline; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public BigDecimal getTemporalPosition() { return temporalPosition; }
    public void setTemporalPosition(BigDecimal temporalPosition) { this.temporalPosition = temporalPosition; }
    public String getTemporalLabel() { return temporalLabel; }
    public void setTemporalLabel(String temporalLabel) { this.temporalLabel = temporalLabel; }
    public String getCalendarSystem() { return calendarSystem; }
    public void setCalendarSystem(String calendarSystem) { this.calendarSystem = calendarSystem; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
