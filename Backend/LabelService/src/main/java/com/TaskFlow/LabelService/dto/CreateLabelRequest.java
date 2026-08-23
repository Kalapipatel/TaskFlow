package com.TaskFlow.LabelService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateLabelRequest(

        @NotBlank(message = "Label name is required")
        @Size(max = 50, message = "Label name cannot exceed 50 characters")
        String name,

        @NotBlank(message = "Label color is required")
        @Pattern(
                regexp = "^#[0-9A-Fa-f]{6}$",
                message = "Label color must be a valid six-digit hex color, for example #E11D48"
        )
        String color,

        @NotNull(message = "Project ID is required")
        UUID projectId
) {
}