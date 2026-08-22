package com.TaskFlow.CommentService.service;

import com.TaskFlow.CommentService.dto.*;
import com.TaskFlow.CommentService.entity.Comment;
import com.TaskFlow.CommentService.exception.*;
import com.TaskFlow.CommentService.repository.CommentCountProjection;
import com.TaskFlow.CommentService.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
        return mapToResponse(savedComment, Collections.emptyList());
    }

    // Manual mapping as requested (no mapper library/component)
    // FIX: Replaces the old method. Ensure the second parameter exists.
    private CommentResponse mapToResponse(Comment comment, List<CommentResponse> replies) {
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
                .content(comment.isDeleted() ? null : comment.getContent())
                .isEdited(comment.isEdited())
                .isDeleted(comment.isDeleted())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .replies(replies) // Maps the passed replies
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }


    @Transactional(readOnly = true)
    public CommentThreadResponse getCommentsForTask(UUID taskId, UUID requesterId, Pageable pageable) {
        // Step 1: Verify requester is project member (Skipped for isolation testing)

        // Step 2: Fetch top-level comments only first
        Page<Comment> topLevelCommentsPage = commentRepository.findTopLevelCommentsByTaskId(taskId, pageable);
        List<Comment> topLevelComments = topLevelCommentsPage.getContent();

        if (topLevelComments.isEmpty()) {
            return new CommentThreadResponse(Collections.emptyList(), 0, pageable.getPageNumber(), pageable.getPageSize());
        }

        // Step 3: Fetch all replies for the returned top-level comment IDs in a single query[cite: 3]
        List<UUID> topLevelIds = topLevelComments.stream().map(Comment::getId).toList();
        List<Comment> allReplies = commentRepository.findRepliesByParentIds(topLevelIds);

        // Step 4: Nest the replies under their parent comments in memory[cite: 3]
        Map<UUID, List<Comment>> repliesByParentId = allReplies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getParentComment().getId()));

        // Step 5: Enrich with author details (Mocked for isolation testing - No Feign calls)
        List<CommentResponse> mappedComments = topLevelComments.stream()
                .map(topLevelComment -> {
                    // Map the replies for this specific parent
                    List<CommentResponse> mappedReplies = repliesByParentId.getOrDefault(topLevelComment.getId(), Collections.emptyList())
                            .stream()
                            .map(reply -> mapToResponse(reply, Collections.emptyList())) // Replies don't have replies
                            .toList();

                    // Map the parent and attach the replies
                    return mapToResponse(topLevelComment, mappedReplies);
                })
                .toList();

        // Step 6: Return paginated CommentThreadResponse[cite: 3]
        return CommentThreadResponse.builder()
                .comments(mappedComments)
                .totalCount(topLevelCommentsPage.getTotalElements())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .build();
    }


    @Transactional
    public CommentResponse updateComment(UUID requesterId, UUID commentId, UpdateCommentRequest req) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found."));

        if (!comment.getAuthorId().equals(requesterId)) {
            throw new NotCommentAuthorException("You can only edit your own comments.");
        }

        if (comment.isDeleted()) {
            throw new CommentAlreadyDeletedException("Cannot edit a deleted comment.");
        }

        comment.setContent(req.content());
        comment.setEdited(true);

        Comment savedComment = commentRepository.save(comment);

        // Map response. If it's a top-level comment, fetch its replies to return the full thread structure.
        List<CommentResponse> mappedReplies = Collections.emptyList();
        if (comment.getParentComment() == null) {
            List<Comment> replies = commentRepository.findRepliesByParentIds(List.of(comment.getId()));
            mappedReplies = replies.stream()
                    .map(reply -> mapToResponse(reply, Collections.emptyList()))
                    .toList();
        }

        return mapToResponse(savedComment, mappedReplies);
    }

    @Transactional
    public void deleteComment(UUID requesterId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found."));

        // Note: Skipping Project Admin/Owner check for isolation testing.
        // Currently strictly validating the author.
        if (!comment.getAuthorId().equals(requesterId)) {
            throw new NotCommentAuthorException("You do not have permission to delete this comment.");
        }

        if (comment.isDeleted()) {
            throw new CommentAlreadyDeletedException("Comment has already been deleted.");
        }

        long replyCount = commentRepository.countByParentCommentId(commentId);

        if (replyCount == 0) {
            // Hard delete if no replies exist
            commentRepository.delete(comment);
        } else {
            // Soft delete if replies exist to preserve thread context
            comment.setDeleted(true);
            comment.setContent(null);
            comment.setDeletedAt(Instant.now());
            commentRepository.save(comment);
        }
    }


    // internal apis
    @Transactional(readOnly = true)
    public Map<UUID, Long> getCommentCounts(List<UUID> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Convert the projection list into a Map<UUID, Long> for the API response[cite: 3]
        return commentRepository.countCommentsByTaskIds(taskIds).stream()
                .collect(Collectors.toMap(
                        CommentCountProjection::getTaskId,
                        CommentCountProjection::getCommentCount
                ));
    }

    @Transactional
    public void deleteAllCommentsForTask(UUID taskId) {
        // Hard delete since the parent task is gone — no need to preserve thread structure[cite: 3]
        commentRepository.deleteAllByTaskId(taskId);
    }

}