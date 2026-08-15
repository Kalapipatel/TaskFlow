package com.TaskFlow.ProjectService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {

    private UUID projectId;
    private String name;
    private UUID ownerId;
    private String projectKey;
}
