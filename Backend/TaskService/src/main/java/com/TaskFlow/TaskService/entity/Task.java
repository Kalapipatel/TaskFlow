package com.TaskFlow.TaskService.entity;

import com.TaskFlow.TaskService.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tasks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tasks_project_number", columnNames = {"project_id", "task_number"})
        },
        indexes = {
                // Redundant project_id index removed; the composite index below covers it.
                @Index(name = "idx_tasks_project_status", columnList = "project_id, status"),
                @Index(name = "idx_tasks_project_assignee", columnList = "project_id, assignee_id"),
                @Index(name = "idx_tasks_assignee_id", columnList = "assignee_id"),
                @Index(name = "idx_tasks_reporter_id", columnList = "reporter_id"),
                @Index(name = "idx_tasks_status", columnList = "status"),
                @Index(name = "idx_tasks_priority", columnList = "priority"),
                @Index(name = "idx_tasks_due_date", columnList = "due_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR) // Crucial for MySQL to save UUIDs as VARCHAR(36) instead of BINARY(255)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Cross-service references (No FKs enforced here as they belong to Project/User services)[cite: 2]
    @Column(name = "project_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID projectId;

    @Column(name = "assignee_id")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID assigneeId;

    @Column(name = "reporter_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "task_number", nullable = false)
    private Integer taskNumber;

    // ElementCollection avoids creating a useless junction entity class for just storing UUIDs.
    // FetchType.LAZY ensures we don't load label IDs on heavy Kanban board queries unless accessed[cite: 2].
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "task_labels",
            joinColumns = @JoinColumn(name = "task_id"),
            indexes = @Index(name = "idx_tl_label_id", columnList = "label_id")
    )
    @Column(name = "label_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Builder.Default
    private Set<UUID> labelIds = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}