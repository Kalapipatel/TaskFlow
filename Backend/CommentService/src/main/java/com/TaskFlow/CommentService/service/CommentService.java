package com.TaskFlow.CommentService.service;

import com.TaskFlow.CommentService.dto.*;
import com.TaskFlow.CommentService.entity.Comment;
import com.TaskFlow.CommentService.exception.CommentNotFoundException;
import com.TaskFlow.CommentService.exception.NestedReplyNotAllowedException;
import com.TaskFlow.CommentService.exception.ParentCommentTaskMismatchException;
import com.TaskFlow.CommentService.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse addComment(UUID authorId, CreateCommentRequest req) {
        // Step 1 & 2: Skipped for isolation testing (Task and Project validation)

        Comment parent = null;

        // Step 3: Validate parent comment if replying
        if (req.parentCommentId() != null) {
            parent = commentRepository.findById(req.parentCommentId())
                    .orElseThrow(() -> new CommentNotFoundException("Parent comment does not exist."));

            if (!parent.getTaskId().equals(req.taskId())) {
                throw new ParentCommentTaskMismatchException("Cannot reply to a comment on a different task.");
            }

            if (parent.getParentComment() != null) {
                throw new NestedReplyNotAllowedException("Nested replies beyond one level are not allowed.");
            }
        }

        // Step 4: Save the comment
        Comment comment = Comment.builder()
                .taskId(req.taskId())
                .authorId(authorId)
                .content(req.content())
                .parentComment(parent)
                .isEdited(false)
                .isDeleted(false)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Step 5: Return 201 Created with CommentResponse
        return mapToResponse(savedComment);
    }

    // Manual mapping as requested (no mapper library/component)
    private CommentResponse mapToResponse(Comment comment) {
        // Mocking UserSummary since User Service is isolated
        UserSummary mockAuthor = new UserSummary(
                comment.getAuthorId(),
                "isolated_user",
                "Isolated User",
                null
        );

        return CommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTaskId())
                .author(mockAuthor)
                .content(comment.getContent())
                .isEdited(comment.isEdited())
                .isDeleted(comment.isDeleted())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .replies(new ArrayList<>()) // New comments have no replies initially
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}