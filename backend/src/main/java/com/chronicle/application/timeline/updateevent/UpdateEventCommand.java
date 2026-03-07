package com.chronicle.application.timeline.updateevent;

import com.chronicle.domain.timeline.ContentType;
import com.chronicle.domain.timeline.TemporalPosition;

public record UpdateEventCommand(String timelineId, String eventId, String title,
                                  String contentText, ContentType contentType,
                                  TemporalPosition temporalPosition) {
}
