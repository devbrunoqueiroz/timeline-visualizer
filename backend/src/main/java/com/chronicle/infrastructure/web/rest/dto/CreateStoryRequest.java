package com.chronicle.infrastructure.web.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStoryRequest(
        @NotBlank String title,
        String description
) {
}
