package com.TaskFlow.LabelService.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateLabelRequest(

        @Size(max = 50, message = "Label name cannot exceed 50 characters")
        @Pattern(regexp = ".*\\S.*", message = "Label name must not be blank")
        String name,

        @Pattern(
                regexp = "^#[0-9A-Fa-f]{6}$",
                message = "Label color must be a valid six-digit hex color, for example #E11D48"
        )
        String color
) {
}
