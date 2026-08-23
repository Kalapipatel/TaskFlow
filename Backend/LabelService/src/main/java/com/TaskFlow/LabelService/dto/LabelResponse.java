package com.TaskFlow.LabelService.dto;

import java.time.Instant;
import java.util.UUID;

public record LabelResponse(
        UUID id,
        String name,
        String color,
        UUID projectId,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}