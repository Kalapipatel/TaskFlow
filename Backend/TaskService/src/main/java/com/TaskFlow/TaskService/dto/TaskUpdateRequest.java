package com.TaskFlow.TaskService.dto;

import com.TaskFlow.TaskService.enums.TaskPriority;
import com.TaskFlow.TaskService.enums.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {

    // All fields are technically optional in an update (PATCH behavior).
    // The service layer will only update the fields that are not null.

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String description;

    private UUID assigneeId;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    private Set<UUID> labelIds;
}