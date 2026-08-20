package com.TaskFlow.CommentService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCommentRequest(
        @NotNull(message = "Task ID is required")
        UUID taskId,

        @NotBlank(message = "Content cannot be empty")
        @Size(max = 10000, message = "Content is too long")
        String content,

        UUID parentCommentId
) {}