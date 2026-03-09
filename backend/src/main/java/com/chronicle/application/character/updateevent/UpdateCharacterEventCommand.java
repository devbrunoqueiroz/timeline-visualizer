package com.chronicle.application.character.updateevent;

import com.chronicle.domain.timeline.ContentType;
import com.chronicle.domain.timeline.TemporalPosition;

public record UpdateCharacterEventCommand(
        String characterId,
        String eventId,
        String title,
        String contentText,
        ContentType contentType,
        TemporalPosition temporalPosition
) {}
