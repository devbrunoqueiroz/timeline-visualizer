package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.domain.timeline.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateEventRequest(
        @NotBlank @Size(max = 300) String title,
        String contentText,
        ContentType contentType,
        @NotNull BigDecimal temporalPosition,
        @NotBlank @Size(max = 500) String temporalLabel,
        String calendarSystem
) {}
