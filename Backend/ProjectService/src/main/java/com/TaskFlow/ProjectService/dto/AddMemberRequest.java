package com.TaskFlow.ProjectService.dto;

import com.TaskFlow.ProjectService.enums.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddMemberRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Role is required")
    private ProjectMemberRole role;
}