package com.TaskFlow.LabelService.controller;

import com.TaskFlow.LabelService.dto.CreateLabelRequest;
import com.TaskFlow.LabelService.dto.LabelResponse;
import com.TaskFlow.LabelService.dto.UpdateLabelRequest;
import com.TaskFlow.LabelService.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping("/labels")
    public ResponseEntity<LabelResponse> createLabel(
            @RequestHeader("X-User-Id") UUID creatorId,
            @Valid @RequestBody CreateLabelRequest request
    ) {
        LabelResponse response = labelService.createLabel(creatorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/labels")
    public ResponseEntity<List<LabelResponse>> getLabelsByProject(
            @PathVariable UUID projectId
    ) {
        return ResponseEntity.ok(
                labelService.getLabelsByProject(projectId)
        );
    }

    @PutMapping("/labels/{labelId}")
    public ResponseEntity<LabelResponse> updateLabel(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID labelId,
            @Valid @RequestBody UpdateLabelRequest request
    ) {
        return ResponseEntity.ok(
                labelService.updateLabel(requesterId, labelId, request)
        );
    }

    @DeleteMapping("/labels/{labelId}")
    public ResponseEntity<Void> deleteLabel(
            @RequestHeader("X-User-Id") UUID requesterId,
            @PathVariable UUID labelId
    ) {
        labelService.deleteLabel(requesterId, labelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/labels/{labelId}")
    public ResponseEntity<LabelResponse> getLabelById(
            @PathVariable UUID labelId
    ) {
        return ResponseEntity.ok(labelService.getLabelById(labelId));
    }
}
