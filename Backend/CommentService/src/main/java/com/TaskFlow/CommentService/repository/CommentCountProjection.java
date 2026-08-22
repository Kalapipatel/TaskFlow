package com.TaskFlow.CommentService.repository;

import java.util.UUID;

public interface CommentCountProjection {
    UUID getTaskId();
    Long getCommentCount();
}