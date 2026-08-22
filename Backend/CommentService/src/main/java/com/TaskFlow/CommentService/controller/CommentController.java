package com.TaskFlow.CommentService.controller;

import com.TaskFlow.CommentService.dto.*;
import com.TaskFlow.CommentService.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @RequestHeader("X-User-Id") UUID authorId,
            @Valid @RequestBody CreateCommentRequest request) {

        CommentResponse response = commentService.addComment(authorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 4. Get Comments for a Task
    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentThreadResponse> getCommentsForTask(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        CommentThreadResponse response = commentService.getCommentsForTask(taskId, requesterId, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        CommentResponse response = commentService.updateComment(requesterId, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID commentId) {

        commentService.deleteComment(requesterId, commentId);
        return ResponseEntity.noContent().build();
    }

}