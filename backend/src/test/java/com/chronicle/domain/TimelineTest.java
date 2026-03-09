package com.chronicle.domain;

import com.chronicle.domain.shared.DomainException;
import com.chronicle.domain.timeline.*;
import com.chronicle.domain.timeline.events.EventAddedToTimeline;
import com.chronicle.domain.timeline.events.TimelineCreated;
import com.chronicle.domain.user.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class TimelineTest {

    private static final TemporalPosition NOW = TemporalPosition.gregorian(Instant.now());
    private static final TemporalPosition CUSTOM_DATE = TemporalPosition.custom(1203L, "Era 2, Ano 1203");
    private static final UserId OWNER = UserId.generate();

    private static Timeline createTimeline(String name, String description) {
        return Timeline.create(name, description, TimelineVisibility.PRIVATE, OWNER);
    }

    @Test
    void shouldCreateTimelineWithValidName() {
        var timeline = createTimeline("Saga Matador de Drakars", "Sete livros");

        assertThat(timeline.getName()).isEqualTo("Saga Matador de Drakars");
        assertThat(timeline.getDescription()).isEqualTo("Sete livros");
        assertThat(timeline.getEvents()).isEmpty();
        assertThat(timeline.getVisibility()).isEqualTo(TimelineVisibility.PRIVATE);
        assertThat(timeline.getId()).isNotNull();
        assertThat(timeline.getCreatedAt()).isNotNull();
        assertThat(timeline.getOwnerId()).isEqualTo(OWNER);
    }

    @Test
    void shouldRegisterTimelineCreatedEvent() {
        var timeline = createTimeline("Test Timeline", "desc");

        assertThat(timeline.getDomainEvents()).hasSize(1);
        assertThat(timeline.getDomainEvents().get(0)).isInstanceOf(TimelineCreated.class);
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> Timeline.create("", "desc", TimelineVisibility.PRIVATE, OWNER))
                .isInstanceOf(DomainException.class)
                .hasMessage("Timeline name cannot be blank");
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> Timeline.create(null, "desc", TimelineVisibility.PRIVATE, OWNER))
                .isInstanceOf(DomainException.class)
                .hasMessage("Timeline name cannot be blank");
    }

    @Test
    void shouldRejectNameExceeding200Chars() {
        var longName = "x".repeat(201);
        assertThatThrownBy(() -> Timeline.create(longName, "desc", TimelineVisibility.PRIVATE, OWNER))
                .isInstanceOf(DomainException.class)
                .hasMessage("Timeline name cannot exceed 200 characters");
    }

    @Test
    void shouldAddEventWithGregorianDate() {
        var timeline = createTimeline("My Timeline", "desc");
        var content = EventContent.text("Something happened");

        var event = timeline.addEvent("First Event", content, NOW);

        assertThat(timeline.getEvents()).hasSize(1);
        assertThat(event.getTitle()).isEqualTo("First Event");
        assertThat(event.getDisplayOrder()).isEqualTo(0);
        assertThat(event.getTemporalPosition().calendarSystem()).isEqualTo(TemporalPosition.GREGORIAN);
    }

    @Test
    void shouldAddEventWithCustomDate() {
        var timeline = createTimeline("Saga Fictícia", "desc");

        var event = timeline.addEvent("Batalha de Aeros", EventContent.text("desc"), CUSTOM_DATE);

        assertThat(event.getTemporalPosition().label()).isEqualTo("Era 2, Ano 1203");
        assertThat(event.getTemporalPosition().calendarSystem()).isEqualTo(TemporalPosition.CUSTOM);
        assertThat(event.getTemporalPosition().position()).isEqualByComparingTo(BigDecimal.valueOf(1203L));
    }

    @Test
    void shouldOrderEventsByTemporalPosition() {
        var timeline = createTimeline("My Timeline", "desc");
        var later   = TemporalPosition.custom(200L, "Ano 200");
        var earlier = TemporalPosition.custom(100L, "Ano 100");

        timeline.addEvent("Later Event",   EventContent.text(""), later);
        timeline.addEvent("Earlier Event", EventContent.text(""), earlier);

        assertThat(timeline.getEvents()).hasSize(2);
    }

    @Test
    void shouldRejectBlankTemporalLabel() {
        assertThatThrownBy(() -> TemporalPosition.custom(BigDecimal.ONE, ""))
                .isInstanceOf(DomainException.class)
                .hasMessage("Temporal position label cannot be blank");
    }

    @Test
    void shouldConvertGregorianPositionBackToInstant() {
        var instant = Instant.parse("2024-06-15T12:00:00Z");
        var position = TemporalPosition.gregorian(instant);

        assertThat(position.toInstant()).isEqualTo(instant);
    }

    @Test
    void shouldRejectToInstantForCustomCalendar() {
        assertThatThrownBy(() -> CUSTOM_DATE.toInstant())
                .isInstanceOf(DomainException.class);
    }

    @Test
    void shouldRegisterEventAddedDomainEvent() {
        var timeline = createTimeline("My Timeline", "desc");
        timeline.pullDomainEvents();

        timeline.addEvent("Event", EventContent.text("text"), NOW);

        assertThat(timeline.getDomainEvents()).hasSize(1);
        assertThat(timeline.getDomainEvents().get(0)).isInstanceOf(EventAddedToTimeline.class);
    }

    @Test
    void shouldRemoveEvent() {
        var timeline = createTimeline("My Timeline", "desc");
        var event = timeline.addEvent("Event 1", EventContent.text("text"), NOW);
        timeline.addEvent("Event 2", EventContent.text("text2"), NOW);

        timeline.removeEvent(event.getId());

        assertThat(timeline.getEvents()).hasSize(1);
        assertThat(timeline.getEvents().get(0).getTitle()).isEqualTo("Event 2");
        assertThat(timeline.getEvents().get(0).getDisplayOrder()).isEqualTo(0);
    }

    @Test
    void shouldUpdateTimeline() {
        var timeline = createTimeline("Old Name", "old desc");

        timeline.update("New Name", "new desc", TimelineVisibility.PUBLIC);

        assertThat(timeline.getName()).isEqualTo("New Name");
        assertThat(timeline.getDescription()).isEqualTo("new desc");
        assertThat(timeline.getVisibility()).isEqualTo(TimelineVisibility.PUBLIC);
    }

    @Test
    void shouldPullAndClearDomainEvents() {
        var timeline = createTimeline("My Timeline", "desc");

        var events = timeline.pullDomainEvents();

        assertThat(events).hasSize(1);
        assertThat(timeline.getDomainEvents()).isEmpty();
    }

    @Test
    void shouldCreateTimelineWithPublicVisibility() {
        var timeline = Timeline.create("Public Timeline", "desc", TimelineVisibility.PUBLIC, OWNER);

        assertThat(timeline.getVisibility()).isEqualTo(TimelineVisibility.PUBLIC);
    }
}
