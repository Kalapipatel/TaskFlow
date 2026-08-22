package com.TaskFlow.CommentService.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record CommentThreadResponse(
        List<CommentResponse> comments,
        long totalCount,
        int page,
        int size
) {}