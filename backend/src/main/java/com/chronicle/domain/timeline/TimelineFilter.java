package com.chronicle.domain.timeline;

import com.chronicle.domain.user.UserId;

import java.util.UUID;

public record TimelineFilter(TimelineVisibility visibility, String nameContains, UUID ownerId) {

    public static TimelineFilter noFilter() {
        return new TimelineFilter(null, null, null);
    }

    public static TimelineFilter byVisibility(TimelineVisibility visibility) {
        return new TimelineFilter(visibility, null, null);
    }

    public static TimelineFilter byOwner(UserId ownerId) {
        return new TimelineFilter(null, null, ownerId.value());
    }
}
