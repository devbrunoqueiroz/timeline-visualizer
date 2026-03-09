package com.chronicle.infrastructure.web.rest.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UpdateCharacterRequest(
        @NotBlank String name,
        String description,
        BigDecimal startPosition,
        BigDecimal endPosition
) {}
