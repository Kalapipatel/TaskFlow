package com.TaskFlow.LabelService.controller;

import com.TaskFlow.LabelService.dto.BatchLabelsRequest;
import com.TaskFlow.LabelService.dto.LabelResponse;
import com.TaskFlow.LabelService.dto.ValidateLabelsRequest;
import com.TaskFlow.LabelService.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/labels")
@RequiredArgsConstructor
public class InternalLabelController {

    private final LabelService labelService;

    @PostMapping("/validate")
    public ResponseEntity<List<LabelResponse>> validateLabels(
            @Valid @RequestBody ValidateLabelsRequest request
    ) {
        return ResponseEntity.ok(
                labelService.validateLabels(
                        request.projectId(),
                        request.labelIds()
                )
        );
    }

    @PostMapping("/batch")
    public ResponseEntity<List<LabelResponse>> getLabelsBatch(
            @Valid @RequestBody BatchLabelsRequest request
    ) {
        return ResponseEntity.ok(
                labelService.getLabelsBatch(request.labelIds())
        );
    }
}
