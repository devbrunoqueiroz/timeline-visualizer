package com.chronicle.application.character.create;

import java.math.BigDecimal;
import java.util.Objects;

public record CreateCharacterCommand(
        String timelineId,
        String name,
        String description,
        BigDecimal startPosition,
        BigDecimal endPosition
) {
    public CreateCharacterCommand {
        Objects.requireNonNull(timelineId, "TimelineId is required");
        Objects.requireNonNull(name, "Name is required");
    }
}
