package com.chronicle.domain.timeline;

public record TimelineFilter(TimelineVisibility visibility, String nameContains) {

    public static TimelineFilter noFilter() {
        return new TimelineFilter(null, null);
    }

    public static TimelineFilter byVisibility(TimelineVisibility visibility) {
        return new TimelineFilter(visibility, null);
    }
}
