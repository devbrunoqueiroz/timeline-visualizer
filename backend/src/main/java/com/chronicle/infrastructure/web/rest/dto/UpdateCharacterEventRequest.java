package com.chronicle.infrastructure.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateCharacterEventRequest(
        @NotBlank String title,
        String contentText,
        String contentType,
        @NotNull BigDecimal temporalPosition,
        @NotBlank String temporalLabel,
        String calendarSystem
) {}
