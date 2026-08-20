package com.TaskFlow.TaskService.dto;

import com.TaskFlow.TaskService.enums.TaskPriority;
import com.TaskFlow.TaskService.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private UUID id;
    private String title;
    private String description;

    // ID references to external services (Frontend will resolve these via API Gateway aggregation if needed)
    private UUID projectId;
    private UUID assigneeId;
    private UUID reporterId;

    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private Integer taskNumber;

    // Pre-formatted string like "PROJ-123" for frontend convenience.
    // The mapper/service will construct this by fetching the project prefix.
    private String taskKey;

    private Set<UUID> labelIds;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
}