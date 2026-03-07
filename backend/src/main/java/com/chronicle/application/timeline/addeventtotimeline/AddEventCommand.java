package com.chronicle.application.timeline.addeventtotimeline;

import com.chronicle.domain.timeline.ContentType;
import com.chronicle.domain.timeline.TemporalPosition;

public record AddEventCommand(String timelineId, String title, String contentText,
                               ContentType contentType, TemporalPosition temporalPosition) {
}
