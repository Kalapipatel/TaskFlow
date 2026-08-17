package com.TaskFlow.ProjectService.controller;

import com.TaskFlow.ProjectService.dto.MembershipResponse;
import com.TaskFlow.ProjectService.dto.ProjectResponse;
import com.TaskFlow.ProjectService.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/projects")
@RequiredArgsConstructor
public class InternalProjectController {

    private final ProjectService projectService;

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectInternal(projectId));
    }

    @GetMapping("/{projectId}/members/{userId}")
    public ResponseEntity<MembershipResponse> verifyMembership(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {

        return ResponseEntity.ok(projectService.verifyMembership(projectId, userId));
    }
}