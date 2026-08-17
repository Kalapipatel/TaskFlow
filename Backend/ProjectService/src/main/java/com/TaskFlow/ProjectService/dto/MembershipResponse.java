package com.TaskFlow.ProjectService.dto;

import com.TaskFlow.ProjectService.enums.ProjectMemberRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class MembershipResponse {
    private UUID projectId;
    private UUID userId;
    private ProjectMemberRole role;
}