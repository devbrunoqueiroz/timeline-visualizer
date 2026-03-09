package com.chronicle.application.character.update;

import java.math.BigDecimal;

public record UpdateCharacterCommand(
        String characterId,
        String name,
        String description,
        BigDecimal startPosition,
        BigDecimal endPosition
) {}
