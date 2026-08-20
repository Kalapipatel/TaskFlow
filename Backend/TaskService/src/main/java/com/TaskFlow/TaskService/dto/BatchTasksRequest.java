package com.TaskFlow.TaskService.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchTasksRequest {

    @NotNull(message = "Task IDs list cannot be null")
    @NotEmpty(message = "Task IDs list cannot be empty")
    private List<UUID> taskIds;
}