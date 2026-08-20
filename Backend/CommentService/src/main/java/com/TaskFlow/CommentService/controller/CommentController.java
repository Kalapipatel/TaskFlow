package com.TaskFlow.CommentService.controller;

import com.TaskFlow.CommentService.dto.*;
import com.TaskFlow.CommentService.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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


}