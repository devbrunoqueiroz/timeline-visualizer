package com.chronicle.infrastructure.web.rest.dto;

import java.math.BigDecimal;

public record CharacterEventResponse(
        String id,
        String title,
        String contentText,
        String contentType,
        BigDecimal temporalPosition,
        String temporalLabel,
        String calendarSystem,
        int displayOrder
) {}
