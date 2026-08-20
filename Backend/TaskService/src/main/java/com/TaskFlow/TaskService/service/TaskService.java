package com.TaskFlow.TaskService.service;


import com.TaskFlow.TaskService.dto.*;
import com.TaskFlow.TaskService.entity.*;
import com.TaskFlow.TaskService.enums.*;
import com.TaskFlow.TaskService.exception.InvalidStatusTransitionException;
import com.TaskFlow.TaskService.exception.TaskNotFoundException;
import com.TaskFlow.TaskService.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;

    @Transactional
    public TaskResponse createTask(TaskCreateRequest req, UUID reporterId) {
        log.info("Creating new task for project: {}", req.getProjectId());

        int nextTaskNumber = taskRepository.findMaxTaskNumberByProjectId(req.getProjectId()).orElse(0) + 1;

        Task task = Task.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .projectId(req.getProjectId())
                .assigneeId(req.getAssigneeId())
                .reporterId(reporterId)
                .priority(req.getPriority() != null ? req.getPriority() : TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .dueDate(req.getDueDate())
                .taskNumber(nextTaskNumber)
                .labelIds(req.getLabelIds() != null ? new HashSet<>(req.getLabelIds()) : new HashSet<>())
                .build();

        task = taskRepository.save(task);

        TaskHistory history = TaskHistory.builder()
                .task(task)
                .changedBy(reporterId)
                .fieldName("created")
                .oldValue(null)
                .newValue(task.getId().toString())
                .build();
        taskHistoryRepository.save(history);

        // TODO (Phase 3): Fetch actual project key via Feign. Using fallback "TEMP" for Phase 2 testing.
        String projectKey = "TEMP";
        String taskKey = projectKey + "-" + nextTaskNumber;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .projectId(task.getProjectId())
                .assigneeId(task.getAssigneeId())
                .reporterId(task.getReporterId())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .taskNumber(task.getTaskNumber())
                .taskKey(taskKey)
                .labelIds(task.getLabelIds())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }


    // Defined per Phase 2 State Machine specifications[cite: 1, 2]
    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = Map.of(
            TaskStatus.TODO, Set.of(TaskStatus.IN_PROGRESS, TaskStatus.IN_REVIEW),
            TaskStatus.IN_PROGRESS, Set.of(TaskStatus.IN_REVIEW, TaskStatus.TODO),
            TaskStatus.IN_REVIEW, Set.of(TaskStatus.DONE, TaskStatus.IN_PROGRESS, TaskStatus.TODO),
            TaskStatus.DONE, Set.of(TaskStatus.IN_PROGRESS, TaskStatus.TODO)
    );


    @Transactional
    public TaskResponse updateTask(UUID requesterId, UUID taskId, TaskUpdateRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // TODO (Phase 3): Verify requester is a member of the project via Project Service Feign client.

        List<TaskHistory> historyEntries = new ArrayList<>();

        if (req.getTitle() != null && !req.getTitle().equals(task.getTitle())) {
            addHistory(historyEntries, task, requesterId, "title", task.getTitle(), req.getTitle());
            task.setTitle(req.getTitle());
        }

        if (req.getDescription() != null && !req.getDescription().equals(task.getDescription())) {
            addHistory(historyEntries, task, requesterId, "description", task.getDescription(), req.getDescription());
            task.setDescription(req.getDescription());
        }

        if (req.getAssigneeId() != null && !req.getAssigneeId().equals(task.getAssigneeId())) {
            // TODO (Phase 3): Verify new assignee is a project member via Feign.
            addHistory(historyEntries, task, requesterId, "assignee_id",
                    task.getAssigneeId() != null ? task.getAssigneeId().toString() : null,
                    req.getAssigneeId().toString());
            task.setAssigneeId(req.getAssigneeId());
        }

        if (req.getPriority() != null && req.getPriority() != task.getPriority()) {
            addHistory(historyEntries, task, requesterId, "priority", task.getPriority().name(), req.getPriority().name());
            task.setPriority(req.getPriority());
        }

        if (req.getDueDate() != null && !req.getDueDate().equals(task.getDueDate())) {
            addHistory(historyEntries, task, requesterId, "due_date",
                    task.getDueDate() != null ? task.getDueDate().toString() : null,
                    req.getDueDate().toString());
            task.setDueDate(req.getDueDate());
        }

        if (req.getLabelIds() != null && !req.getLabelIds().equals(task.getLabelIds())) {
            // TODO (Phase 3): Validate label IDs belong to project via Label Service Feign client.
            addHistory(historyEntries, task, requesterId, "labels",
                    task.getLabelIds().toString(), req.getLabelIds().toString());
            task.setLabelIds(new HashSet<>(req.getLabelIds()));
        }

        if (req.getStatus() != null && req.getStatus() != task.getStatus()) {
            validateStatusTransition(task.getStatus(), req.getStatus());
            addHistory(historyEntries, task, requesterId, "status", task.getStatus().name(), req.getStatus().name());

            // Handle completed_at logic
            if (req.getStatus() == TaskStatus.DONE) {
                task.setCompletedAt(Instant.now());
            } else if (task.getStatus() == TaskStatus.DONE) {
                task.setCompletedAt(null); // Reopened
            }

            task.setStatus(req.getStatus());
        }

        // Save atomically
        if (!historyEntries.isEmpty()) {
            taskHistoryRepository.saveAll(historyEntries);
            task = taskRepository.save(task);
        }

        String projectKey = "TEMP"; // Fallback for Phase 2
        String taskKey = projectKey + "-" + task.getTaskNumber();

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .projectId(task.getProjectId())
                .assigneeId(task.getAssigneeId())
                .reporterId(task.getReporterId())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .taskNumber(task.getTaskNumber())
                .taskKey(taskKey)
                .labelIds(task.getLabelIds())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }

    private void validateStatusTransition(TaskStatus current, TaskStatus next) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Collections.emptySet()).contains(next)) {
            throw new InvalidStatusTransitionException(
                    current.name(),
                    next.name(),
                    ALLOWED_TRANSITIONS.get(current).toString()
            );
        }
    }

    private void addHistory(List<TaskHistory> list, Task task, UUID requesterId, String field, String oldVal, String newVal) {
        list.add(TaskHistory.builder()
                .task(task)
                .changedBy(requesterId)
                .fieldName(field)
                .oldValue(oldVal)
                .newValue(newVal)
                .build());
    }


    @Transactional(readOnly = true)
    public BoardResponse getKanbanBoard(UUID projectId, int doneOffset, int doneLimit) {
        // TODO (Phase 3): Add Feign validation to ensure project exists and requester is a member

        List<Task> activeTasks = taskRepository.findActiveBoardTasks(projectId);
        List<Task> doneTasks = taskRepository.findDoneBoardTasks(projectId, doneLimit, doneOffset);

        Map<TaskStatus, List<TaskSummary>> groupedActive = activeTasks.stream()
                .map(this::mapToSummary)
                .collect(Collectors.groupingBy(TaskSummary::getStatus));

        return BoardResponse.builder()
                .todo(groupedActive.getOrDefault(TaskStatus.TODO, List.of()))
                .inProgress(groupedActive.getOrDefault(TaskStatus.IN_PROGRESS, List.of()))
                .inReview(groupedActive.getOrDefault(TaskStatus.IN_REVIEW, List.of()))
                .done(doneTasks.stream().map(this::mapToSummary).toList())
                .build();
    }

    private TaskSummary mapToSummary(Task task) {
        // TODO (Phase 3): Fetch actual projectKey from Project Service.
        String projectKey = "TEMP";

        boolean isOverdue = task.getDueDate() != null &&
                task.getDueDate().isBefore(LocalDate.now()) &&
                task.getStatus() != TaskStatus.DONE;

        return TaskSummary.builder()
                .id(task.getId())
                .taskKey(projectKey + "-" + task.getTaskNumber())
                .title(task.getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .assigneeId(task.getAssigneeId())
                .labelIds(task.getLabelIds())
                .isOverdue(isOverdue)
                .build();
    }

}