package com.TaskFlow.TaskService.dto;

import com.TaskFlow.TaskService.enums.TaskPriority;
import com.TaskFlow.TaskService.enums.TaskStatus;
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
public class TaskSummary {
    private UUID id;
    private String taskKey;
    private String title;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private UUID assigneeId; // TODO (Phase 3): Replace with UserSummary after Feign integration
    private Set<UUID> labelIds; // TODO (Phase 3): Replace with LabelSummary after Feign integration
    private boolean isOverdue;
}