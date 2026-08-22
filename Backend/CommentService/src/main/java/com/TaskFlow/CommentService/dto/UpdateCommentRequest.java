package com.TaskFlow.CommentService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(
        @NotBlank(message = "Content cannot be empty")
        @Size(max = 10000, message = "Content is too long")
        String content
) {}