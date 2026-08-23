package com.TaskFlow.LabelService.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ValidateLabelsRequest(
        @NotNull(message = "Project ID is required")
        UUID projectId,

        @NotNull(message = "Label IDs list is required")
        List<@NotNull(message = "Label ID cannot be null") UUID> labelIds
) {
}
