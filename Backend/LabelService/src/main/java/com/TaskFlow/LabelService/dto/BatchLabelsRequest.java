package com.TaskFlow.LabelService.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BatchLabelsRequest(
        @NotNull(message = "Label IDs list is required")
        List<@NotNull(message = "Label ID cannot be null") UUID> labelIds
) {
}
