package com.TaskFlow.ProjectService.dto;

import com.TaskFlow.ProjectService.enums.ProjectMemberRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ProjectMemberResponse {
    private UUID userId;
    private String fullName; // Enriched via User Service Feign call
    private String email;
    private ProjectMemberRole role;
    private Instant joinedAt;
}
