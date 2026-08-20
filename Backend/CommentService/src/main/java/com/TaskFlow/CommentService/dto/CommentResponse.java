package com.TaskFlow.CommentService.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private UUID taskId;
    private UserSummary author;
    private String content;
    private boolean isEdited;
    private boolean isDeleted;
    private UUID parentCommentId;
    private List<CommentResponse> replies;
    private Instant createdAt;
    private Instant updatedAt;
}
