package com.TaskFlow.TaskService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskHistoryResponse {

    private UUID id;
    private UUID taskId;
    private UUID changedBy;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Instant changedAt;
}