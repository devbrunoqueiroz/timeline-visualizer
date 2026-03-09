package com.chronicle.application.character.addevent;

import com.chronicle.domain.timeline.ContentType;
import com.chronicle.domain.timeline.TemporalPosition;

public record AddCharacterEventCommand(
        String characterId,
        String title,
        String contentText,
        ContentType contentType,
        TemporalPosition temporalPosition
) {}
