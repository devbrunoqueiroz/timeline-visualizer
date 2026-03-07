package com.chronicle.application.connection.createconnection;

import com.chronicle.application.narrative.NarrativeValidationService;
import com.chronicle.application.shared.UseCase;
import com.chronicle.domain.connection.ConnectionRepository;
import com.chronicle.domain.connection.ConnectionType;
import com.chronicle.domain.connection.TimelineConnection;
import com.chronicle.domain.narrative.NarrativeValidationResult;
import com.chronicle.domain.timeline.TimelineEvent;
import com.chronicle.domain.timeline.TimelineEventId;
import com.chronicle.domain.timeline.TimelineFilter;
import com.chronicle.domain.timeline.TimelineRepository;

import java.util.List;
import java.util.Optional;

public class CreateConnectionUseCase implements UseCase<CreateConnectionCommand, CreateConnectionResult> {

    private final ConnectionRepository connectionRepository;
    private final TimelineRepository timelineRepository;
    private final NarrativeValidationService validationService;

    public CreateConnectionUseCase(ConnectionRepository connectionRepository,
                                    TimelineRepository timelineRepository,
                                    NarrativeValidationService validationService) {
        this.connectionRepository = connectionRepository;
        this.timelineRepository = timelineRepository;
        this.validationService = validationService;
    }

    @Override
    public CreateConnectionResult execute(CreateConnectionCommand command) {
        var sourceId = TimelineEventId.of(command.sourceEventId());
        var targetId = TimelineEventId.of(command.targetEventId());
        var type = command.connectionType() != null ? command.connectionType() : ConnectionType.REFERENCE;

        var connection = TimelineConnection.create(sourceId, targetId, command.description(), type);
        connectionRepository.save(connection);

        var validations = runValidation(connection, sourceId, targetId);
        return CreateConnectionResult.from(connection, validations);
    }

    private List<NarrativeValidationResult> runValidation(TimelineConnection connection,
                                                            TimelineEventId sourceId,
                                                            TimelineEventId targetId) {
        var sourceEvent = findEvent(sourceId);
        var targetEvent = findEvent(targetId);
        if (sourceEvent.isEmpty() || targetEvent.isEmpty()) return List.of();

        return validationService.validate(
                connection.getId().value().toString(),
                connection.getConnectionType(),
                sourceEvent.get().getTemporalPosition(),
                targetEvent.get().getTemporalPosition()
        );
    }

    private Optional<TimelineEvent> findEvent(TimelineEventId eventId) {
        return timelineRepository.findAll(TimelineFilter.noFilter()).stream()
                .flatMap(t -> t.getEvents().stream())
                .filter(e -> e.getId().equals(eventId))
                .findFirst();
    }
}
