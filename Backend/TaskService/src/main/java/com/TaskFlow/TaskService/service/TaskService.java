package com.TaskFlow.TaskService.service;


import com.TaskFlow.TaskService.dto.*;
import com.TaskFlow.TaskService.entity.*;
import com.TaskFlow.TaskService.enums.*;
import com.TaskFlow.TaskService.exception.InvalidStatusTransitionException;
import com.TaskFlow.TaskService.exception.TaskNotFoundException;
import com.TaskFlow.TaskService.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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


    @Transactional(readOnly = true)
    public TaskResponse getTaskDetail(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        return buildTaskResponse(task);
    }

    private TaskResponse buildTaskResponse(Task task) {
        String projectKey = "TEMP";
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
                .taskKey(projectKey + "-" + task.getTaskNumber())
                .labelIds(task.getLabelIds())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }


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

        List<Task> activeTasks = taskRepository.findActiveBoardTasks(projectId.toString());
        List<Task> doneTasks = taskRepository.findDoneBoardTasks(projectId.toString(), doneLimit, doneOffset);

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


    @Transactional(readOnly = true)
    public Page<TaskSummary> searchTasks(
            UUID projectId, TaskStatus status, TaskPriority priority,
            UUID assigneeId, String search, LocalDate dueBefore,
            UUID labelId, Pageable pageable) {

        // TODO (Phase 3): Add Feign validation to ensure project exists and requester is a member

        // Base criteria: Task must belong to the project
        Specification<Task> spec = Specification.where(TaskSpecification.hasProjectId(projectId));

        // Dynamically chain filters only if parameters are explicitly provided
        if (status != null) spec = spec.and(TaskSpecification.hasStatus(status));
        if (priority != null) spec = spec.and(TaskSpecification.hasPriority(priority));
        if (assigneeId != null) spec = spec.and(TaskSpecification.hasAssigneeId(assigneeId));
        if (search != null && !search.isBlank()) spec = spec.and(TaskSpecification.titleContainsIgnoreCase(search));
        if (dueBefore != null) spec = spec.and(TaskSpecification.dueBefore(dueBefore));
        if (labelId != null) spec = spec.and(TaskSpecification.hasLabelId(labelId));

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        return taskPage.map(this::mapToSummary); // Utilizing existing mapToSummary method
    }


    @Transactional
    public void attachLabels(UUID requesterId, UUID taskId, AttachLabelsRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // TODO (Phase 3): Verify requester is a member of the project via Project Service Feign client.[cite: 1]
        // TODO (Phase 3): Call Label Service to validate all label IDs belong to this project.

        Set<UUID> currentLabels = task.getLabelIds();
        List<TaskHistory> historyEntries = new ArrayList<>();

        for (UUID labelId : req.getLabelIds()) {
            // Only add and record history if the label isn't already attached (idempotent)[cite: 1]
            if (!currentLabels.contains(labelId)) {
                currentLabels.add(labelId);

                historyEntries.add(TaskHistory.builder()
                        .task(task)
                        .changedBy(requesterId)
                        .fieldName("label_id")
                        .oldValue(null)
                        .newValue(labelId.toString())
                        .build());
            }
        }

        if (!historyEntries.isEmpty()) {
            taskHistoryRepository.saveAll(historyEntries);
            taskRepository.save(task); // Hibernate automatically manages the task_labels junction table rows
        }
    }

    @Transactional
    public void removeLabel(UUID requesterId, UUID taskId, UUID labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // TODO (Phase 3): Verify requester is a member of the project via Project Service Feign client.[cite: 1]

        if (task.getLabelIds().contains(labelId)) {
            task.getLabelIds().remove(labelId);

            TaskHistory history = TaskHistory.builder()
                    .task(task)
                    .changedBy(requesterId)
                    .fieldName("label_id")
                    .oldValue(labelId.toString())
                    .newValue(null)
                    .build();

            taskHistoryRepository.save(history);
            taskRepository.save(task); // Hibernate automatically removes the row from task_labels
        }
    }

    @Transactional
    public void deleteTask(UUID requesterId, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // TODO (Phase 3): Verify requester role via Project Service Feign Client.
        // MembershipResponse membership = projectClient.getMembership(task.getProjectId(), requesterId);
        // if (!"ADMIN".equals(membership.role()) && !"OWNER".equals(membership.role())) {
        //     throw new InsufficientRoleException("ADMIN or OWNER");
        // }

        // TODO (Phase 3): Synchronous Saga Pattern for Comment Cleanup.
        // Since you are not using Kafka, we must synchronously instruct the Comment Service
        // to delete its orphaned records before we delete the task.
        // try {
        //     commentClient.deleteCommentsForTask(taskId);
        // } catch (Exception e) {
        //     throw new ServiceUnavailableException("Comment Service"); // Abort task deletion if cleanup fails[cite: 1]
        // }

        // Delete the task.
        // Note: Because task_labels and task_history foreign keys were defined with
        // ON DELETE CASCADE in the database schema, Hibernate/PostgreSQL will automatically
        // wipe the associated audit logs and label associations for us.[cite: 2]
        taskRepository.delete(task);
    }


    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getTaskHistory(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }

        return taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId).stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    private TaskHistoryResponse mapToHistoryResponse(TaskHistory history) {
        String humanReadable = "Updated " + history.getFieldName();

        if ("status".equals(history.getFieldName())) {
            humanReadable = "moved this from " + history.getOldValue() + " to " + history.getNewValue();
        } else if ("assignee_id".equals(history.getFieldName())) {
            if (history.getNewValue() == null) {
                humanReadable = "unassigned this task";
            } else {
                // TODO (Phase 3): Map UUID to human-readable names via User Service batch call[cite: 2]
                humanReadable = "assigned this to " + history.getNewValue();
            }
        } else if ("created".equals(history.getFieldName())) {
            humanReadable = "created this task";
        } else if ("label_id".equals(history.getFieldName())) {
            if (history.getNewValue() == null) {
                humanReadable = "removed a label";
            } else {
                humanReadable = "added a label";
            }
        }

        return TaskHistoryResponse.builder()
                .id(history.getId())
                .changedBy(history.getChangedBy())
                .fieldName(history.getFieldName())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .changedAt(history.getChangedAt())
                .humanReadable(humanReadable)
                .build();
    }

    /**
     * PUT /api/v1/tasks/{taskId}/status
     * Dedicated endpoint for status updates to enforce strict state machine rules.
     */
    @Transactional
    public TaskResponse updateTaskStatus(UUID requesterId, UUID taskId, UpdateTaskStatusRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (task.getStatus() == req.getStatus()) {
            return buildTaskResponse(task); // No-op if status already matches
        }

        // Enforce the strict state machine transitions
        validateStatusTransition(task.getStatus(), req.getStatus());

        List<TaskHistory> history = new ArrayList<>();
        addHistory(history, task, requesterId, "status", task.getStatus().name(), req.getStatus().name());

        if (req.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(Instant.now());
        } else if (task.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(null); // Reopen logic
        }

        task.setStatus(req.getStatus());
        taskHistoryRepository.saveAll(history);
        task = taskRepository.save(task);

        // TODO (Phase 3): Publish task-service.task.status.changed Kafka event

        return buildTaskResponse(task);
    }

    /**
     * PUT /api/v1/tasks/{taskId}/assign
     */
    @Transactional
    public TaskResponse assignTask(UUID requesterId, UUID taskId, AssignTaskRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        // TODO (Phase 3): Call Project Service via Feign to ensure Assignee is a member

        if (req.getAssigneeId().equals(task.getAssigneeId())) {
            return buildTaskResponse(task);
        }

        List<TaskHistory> history = new ArrayList<>();
        String oldVal = task.getAssigneeId() != null ? task.getAssigneeId().toString() : null;
        addHistory(history, task, requesterId, "assignee_id", oldVal, req.getAssigneeId().toString());

        task.setAssigneeId(req.getAssigneeId());
        taskHistoryRepository.saveAll(history);
        task = taskRepository.save(task);

        // TODO (Phase 3): Publish task-service.task.assigned Kafka event

        return buildTaskResponse(task);
    }

    /**
     * DELETE /api/v1/tasks/{taskId}/assign
     */
    @Transactional
    public TaskResponse unassignTask(UUID requesterId, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (task.getAssigneeId() == null) {
            return buildTaskResponse(task);
        }

        List<TaskHistory> history = new ArrayList<>();
        addHistory(history, task, requesterId, "assignee_id", task.getAssigneeId().toString(), null);

        task.setAssigneeId(null);
        taskHistoryRepository.saveAll(history);
        task = taskRepository.save(task);

        return buildTaskResponse(task);
    }



    /**
     * INTERNAL API: Fetches a single task to verify existence and return context.
     * Used by Comment Service to ensure a task exists before allowing comments[cite: 2].
     */
    @Transactional(readOnly = true)
    public TaskSummary getInternalTask(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        return mapToSummary(task);
    }

    /**
     * INTERNAL API: Batch fetches tasks to avoid N+1 Feign calls.
     * Essential for performance when other services need metadata for multiple tasks at once[cite: 2].
     */
    @Transactional(readOnly = true)
    public List<TaskSummary> getTasksBatch(List<UUID> taskIds) {
        List<Task> tasks = taskRepository.findAllById(taskIds);

        // Return mapped summaries. We don't throw an error if some IDs are missing,
        // we just return what was found to allow graceful degradation[cite: 2].
        return tasks.stream()
                .map(this::mapToSummary)
                .toList();
    }

}