package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.domain.connection.ConnectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateConnectionRequest(
        @NotBlank String sourceEventId,
        @NotBlank String targetEventId,
        String description,
        @NotNull ConnectionType connectionType
) {}
