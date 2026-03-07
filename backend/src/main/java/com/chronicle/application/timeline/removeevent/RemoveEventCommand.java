package com.chronicle.application.timeline.removeevent;

public record RemoveEventCommand(String timelineId, String eventId) {
}
