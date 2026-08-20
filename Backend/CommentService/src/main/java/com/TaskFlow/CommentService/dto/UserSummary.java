package com.TaskFlow.CommentService.dto;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String username,
        String fullName,
        String profilePictureUrl
) {}