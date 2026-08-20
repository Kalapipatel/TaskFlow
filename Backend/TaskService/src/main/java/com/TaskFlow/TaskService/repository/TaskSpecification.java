package com.TaskFlow.TaskService.repository;

import com.TaskFlow.TaskService.entity.Task;
import com.TaskFlow.TaskService.enums.TaskPriority;
import com.TaskFlow.TaskService.enums.TaskStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> hasProjectId(UUID projectId) {
        // We cast to String to ensure MySQL binary matching doesn't silently fail, as fixed earlier
        return (root, query, cb) -> cb.equal(root.get("projectId"), projectId.toString());
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasAssigneeId(UUID assigneeId) {
        return (root, query, cb) -> cb.equal(root.get("assigneeId"), assigneeId.toString());
    }

    public static Specification<Task> titleContainsIgnoreCase(String search) {
        // INTERVIEW NOTE: For SDE-1 level, JPA lower/like combo (essentially ILIKE) is fine.
        // For production scale full-text search, mention adding a PostgreSQL GIN index:
        // CREATE INDEX idx_tasks_title_fts ON tasks USING gin(to_tsvector('english', title));
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%");
    }

    public static Specification<Task> dueBefore(LocalDate date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dueDate"), date);
    }

    public static Specification<Task> hasLabelId(UUID labelId) {
        return (root, query, cb) -> {
            // Safely join the @ElementCollection of label IDs
            Join<Task, String> labels = root.join("labelIds");
            return cb.equal(labels, labelId.toString());
        };
    }
}