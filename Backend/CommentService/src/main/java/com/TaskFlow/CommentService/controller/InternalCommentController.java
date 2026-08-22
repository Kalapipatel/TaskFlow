package com.TaskFlow.CommentService.controller;

import com.TaskFlow.CommentService.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/comments")
@RequiredArgsConstructor
public class InternalCommentController {

    private final CommentService commentService;

    // Called by Task Service to display comment counts on the Kanban board
    @GetMapping("/count")
    public ResponseEntity<Map<UUID, Long>> getCommentCounts(@RequestParam List<UUID> taskIds) {
        return ResponseEntity.ok(commentService.getCommentCounts(taskIds));
    }

    // Called by Task Service when a task is deleted to clean up orphans
    @DeleteMapping("/task/{taskId}")
    public ResponseEntity<Void> deleteAllCommentsForTask(@PathVariable UUID taskId) {
        commentService.deleteAllCommentsForTask(taskId);
        return ResponseEntity.noContent().build();
    }
}