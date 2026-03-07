package com.chronicle.infrastructure.persistence.jpa;

import com.chronicle.domain.timeline.*;

import java.time.Instant;
import java.util.List;

public class TimelineMapper {

    public TimelineEntity toEntity(Timeline timeline) {
        var entity = new TimelineEntity();
        entity.setId(timeline.getId().value());
        entity.setName(timeline.getName());
        entity.setDescription(timeline.getDescription());
        entity.setVisibility(timeline.getVisibility().name());
        entity.setCreatedAt(timeline.getCreatedAt());
        entity.setUpdatedAt(timeline.getUpdatedAt());

        var eventEntities = timeline.getEvents().stream()
                .map(event -> toEventEntity(event, entity))
                .toList();
        entity.getEvents().clear();
        entity.getEvents().addAll(eventEntities);
        return entity;
    }

    public Timeline toDomain(TimelineEntity entity) {
        List<TimelineEvent> events = entity.getEvents().stream()
                .map(this::toEventDomain)
                .toList();
        return Timeline.reconstitute(
                new TimelineId(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                TimelineVisibility.valueOf(entity.getVisibility()),
                events,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TimelineEventEntity toEventEntity(TimelineEvent event, TimelineEntity timelineEntity) {
        var entity = new TimelineEventEntity();
        entity.setId(event.getId().value());
        entity.setTimeline(timelineEntity);
        entity.setTitle(event.getTitle());
        entity.setContentText(event.getContent().text());
        entity.setContentType(event.getContent().type().name());
        entity.setTemporalPosition(event.getTemporalPosition().position());
        entity.setTemporalLabel(event.getTemporalPosition().label());
        entity.setCalendarSystem(event.getTemporalPosition().calendarSystem());
        entity.setDisplayOrder(event.getDisplayOrder());
        return entity;
    }

    private TimelineEvent toEventDomain(TimelineEventEntity entity) {
        var content = new EventContent(
                entity.getContentText(),
                ContentType.valueOf(entity.getContentType()),
                java.util.Map.of()
        );
        var temporalPosition = new TemporalPosition(
                entity.getTemporalPosition(),
                entity.getTemporalLabel(),
                entity.getCalendarSystem()
        );
        return TimelineEvent.reconstitute(
                new TimelineEventId(entity.getId()),
                entity.getTitle(),
                content,
                temporalPosition,
                entity.getDisplayOrder()
        );
    }
}
