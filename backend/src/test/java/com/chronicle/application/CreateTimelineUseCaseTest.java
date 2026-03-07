package com.chronicle.application;

import com.chronicle.application.shared.DomainEventPublisher;
import com.chronicle.application.timeline.createtimeline.CreateTimelineCommand;
import com.chronicle.application.timeline.createtimeline.CreateTimelineUseCase;
import com.chronicle.domain.timeline.Timeline;
import com.chronicle.domain.timeline.TimelineRepository;
import com.chronicle.domain.timeline.TimelineVisibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTimelineUseCaseTest {

    @Mock
    private TimelineRepository timelineRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private CreateTimelineUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateTimelineUseCase(timelineRepository, eventPublisher);
    }

    @Test
    void shouldCreateAndSaveTimeline() {
        var command = new CreateTimelineCommand("My Timeline", "A description", TimelineVisibility.PRIVATE);

        var result = useCase.execute(command);

        assertThat(result.name()).isEqualTo("My Timeline");
        assertThat(result.description()).isEqualTo("A description");
        assertThat(result.visibility()).isEqualTo(TimelineVisibility.PRIVATE);
        assertThat(result.id()).isNotNull();
        verify(timelineRepository).save(any(Timeline.class));
    }

    @Test
    void shouldPublishDomainEvents() {
        var command = new CreateTimelineCommand("Timeline", "desc", TimelineVisibility.PUBLIC);

        useCase.execute(command);

        verify(eventPublisher, atLeastOnce()).publishAll(any());
    }
}
