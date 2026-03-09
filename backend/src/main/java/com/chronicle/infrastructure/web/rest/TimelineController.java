package com.chronicle.infrastructure.web.rest;

import com.chronicle.application.timeline.addeventtotimeline.AddEventCommand;
import com.chronicle.application.timeline.addeventtotimeline.AddEventToTimelineUseCase;
import com.chronicle.application.timeline.createtimeline.CreateTimelineCommand;
import com.chronicle.application.timeline.createtimeline.CreateTimelineUseCase;
import com.chronicle.application.timeline.deletetimeline.DeleteTimelineCommand;
import com.chronicle.application.timeline.deletetimeline.DeleteTimelineUseCase;
import com.chronicle.application.timeline.gettimeline.GetTimelineQuery;
import com.chronicle.application.timeline.gettimeline.GetTimelineUseCase;
import com.chronicle.application.timeline.listtimelines.ListTimelinesQuery;
import com.chronicle.application.timeline.listtimelines.ListTimelinesUseCase;
import com.chronicle.application.timeline.removeevent.RemoveEventCommand;
import com.chronicle.application.timeline.removeevent.RemoveEventUseCase;
import com.chronicle.application.timeline.updateevent.UpdateEventCommand;
import com.chronicle.application.timeline.updateevent.UpdateEventUseCase;
import com.chronicle.application.timeline.updatetimeline.UpdateTimelineCommand;
import com.chronicle.application.timeline.updatetimeline.UpdateTimelineUseCase;
import com.chronicle.domain.timeline.ContentType;
import com.chronicle.domain.timeline.TemporalPosition;
import com.chronicle.domain.timeline.TimelineId;
import com.chronicle.domain.user.UserId;
import com.chronicle.infrastructure.security.ChronicleUserDetails;
import com.chronicle.infrastructure.web.rest.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timelines")
public class TimelineController {

    private final CreateTimelineUseCase createTimeline;
    private final GetTimelineUseCase getTimeline;
    private final ListTimelinesUseCase listTimelines;
    private final UpdateTimelineUseCase updateTimeline;
    private final DeleteTimelineUseCase deleteTimeline;
    private final AddEventToTimelineUseCase addEvent;
    private final UpdateEventUseCase updateEvent;
    private final RemoveEventUseCase removeEvent;

    public TimelineController(CreateTimelineUseCase createTimeline,
                               GetTimelineUseCase getTimeline,
                               ListTimelinesUseCase listTimelines,
                               UpdateTimelineUseCase updateTimeline,
                               DeleteTimelineUseCase deleteTimeline,
                               AddEventToTimelineUseCase addEvent,
                               UpdateEventUseCase updateEvent,
                               RemoveEventUseCase removeEvent) {
        this.createTimeline = createTimeline;
        this.getTimeline = getTimeline;
        this.listTimelines = listTimelines;
        this.updateTimeline = updateTimeline;
        this.deleteTimeline = deleteTimeline;
        this.addEvent = addEvent;
        this.updateEvent = updateEvent;
        this.removeEvent = removeEvent;
    }

    @GetMapping
    public List<TimelineSummaryResponse> list(@AuthenticationPrincipal ChronicleUserDetails user) {
        return listTimelines.execute(ListTimelinesQuery.forOwner(UserId.of(user.getUserId()))).stream()
                .map(TimelineSummaryResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimelineResponse create(@RequestBody @Valid CreateTimelineRequest request,
                                   @AuthenticationPrincipal ChronicleUserDetails user) {
        var command = new CreateTimelineCommand(request.name(), request.description(), request.visibility(),
                UserId.of(user.getUserId()));
        var result = createTimeline.execute(command);
        var view = getTimeline.execute(new GetTimelineQuery(TimelineId.of(result.id())));
        return TimelineResponse.from(view);
    }

    @GetMapping("/{id}")
    public TimelineResponse getById(@PathVariable String id) {
        var view = getTimeline.execute(new GetTimelineQuery(TimelineId.of(id)));
        return TimelineResponse.from(view);
    }

    @PutMapping("/{id}")
    public TimelineResponse update(@PathVariable String id,
                                    @RequestBody @Valid UpdateTimelineRequest request) {
        var command = new UpdateTimelineCommand(id, request.name(), request.description(), request.visibility());
        updateTimeline.execute(command);
        var view = getTimeline.execute(new GetTimelineQuery(TimelineId.of(id)));
        return TimelineResponse.from(view);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        deleteTimeline.execute(new DeleteTimelineCommand(id));
    }

    @PostMapping("/{id}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public TimelineEventResponse addEvent(@PathVariable String id,
                                           @RequestBody @Valid CreateEventRequest request) {
        var contentType = request.contentType() != null ? request.contentType() : ContentType.TEXT;
        var temporalPosition = new TemporalPosition(
                request.temporalPosition(), request.temporalLabel(),
                request.calendarSystem() != null ? request.calendarSystem() : TemporalPosition.CUSTOM);
        var command = new AddEventCommand(id, request.title(),
                request.contentText() != null ? request.contentText() : "",
                contentType, temporalPosition);
        var result = addEvent.execute(command);
        return new TimelineEventResponse(result.id(), result.title(), result.contentText(),
                result.contentType(), result.temporalPosition().position(),
                result.temporalPosition().label(), result.temporalPosition().calendarSystem(),
                result.displayOrder());
    }

    @PutMapping("/{id}/events/{eid}")
    public TimelineEventResponse updateEvent(@PathVariable String id,
                                              @PathVariable String eid,
                                              @RequestBody @Valid UpdateEventRequest request) {
        var contentType = request.contentType() != null ? request.contentType() : ContentType.TEXT;
        var temporalPosition = new TemporalPosition(
                request.temporalPosition(), request.temporalLabel(),
                request.calendarSystem() != null ? request.calendarSystem() : TemporalPosition.CUSTOM);
        var command = new UpdateEventCommand(id, eid, request.title(),
                request.contentText() != null ? request.contentText() : "",
                contentType, temporalPosition);
        var result = updateEvent.execute(command);
        return new TimelineEventResponse(result.id(), result.title(), result.contentText(),
                result.contentType(), result.temporalPosition().position(),
                result.temporalPosition().label(), result.temporalPosition().calendarSystem(),
                result.displayOrder());
    }

    @DeleteMapping("/{id}/events/{eid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEvent(@PathVariable String id, @PathVariable String eid) {
        removeEvent.execute(new RemoveEventCommand(id, eid));
    }
}
