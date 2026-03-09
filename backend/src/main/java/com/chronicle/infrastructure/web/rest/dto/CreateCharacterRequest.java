package com.chronicle.infrastructure.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCharacterRequest(
        @NotBlank @Size(max = 200) String name,
        String description,
        BigDecimal startPosition,
        BigDecimal endPosition
) {}
