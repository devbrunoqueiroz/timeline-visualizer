package com.chronicle.application.timeline.updatetimeline;

import com.chronicle.domain.timeline.TimelineVisibility;

public record UpdateTimelineCommand(String timelineId, String name, String description,
                                     TimelineVisibility visibility) {
}
