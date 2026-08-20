package com.TaskFlow.CommentService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_task_created", columnList = "task_id, created_at"),
        @Index(name = "idx_comments_task_id", columnList = "task_id"),
        @Index(name = "idx_comments_author_id", columnList = "author_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "task_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID taskId;

    @Column(name = "author_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID authorId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}