package com.TaskFlow.ProjectService.dto;

import com.TaskFlow.ProjectService.enums.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @NotNull(message = "Role is required")
    private ProjectMemberRole role;
}