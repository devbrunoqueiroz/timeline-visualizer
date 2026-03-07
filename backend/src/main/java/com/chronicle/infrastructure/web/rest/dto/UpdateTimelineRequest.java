package com.chronicle.infrastructure.web.rest.dto;

import com.chronicle.domain.timeline.TimelineVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTimelineRequest(
        @NotBlank @Size(max = 200) String name,
        String description,
        TimelineVisibility visibility
) {}
