package com.TaskFlow.ProjectService.controller;

import com.TaskFlow.ProjectService.dto.*;
import com.TaskFlow.ProjectService.security.UserPrincipal;
import com.TaskFlow.ProjectService.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateProjectRequest request) {

        ProjectResponse response = projectService.createProject(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(projectService.getMyProjects(principal.userId()));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request) {

        return ResponseEntity.ok(projectService.updateProject(principal.userId(), projectId, request));
    }

    @PutMapping("/{projectId}/archive")
    public ResponseEntity<Void> archiveProject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId) {

        projectService.archiveProject(principal.userId(), projectId);
        return ResponseEntity.noContent().build();
    }

    // --- MEMBERSHIP ENDPOINTS ---

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectMemberResponse> addMember(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @Valid @RequestBody AddMemberRequest request) {

        ProjectMemberResponse response = projectService.addMember(principal.userId(), projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId) {

        return ResponseEntity.ok(projectService.getMembers(principal.userId(), projectId));
    }

    @PutMapping("/{projectId}/members/{userId}/role")
    public ResponseEntity<Void> changeRole(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeRoleRequest request) {

        projectService.changeRole(principal.userId(), projectId, userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {

        projectService.removeMember(principal.userId(), projectId, userId);
        return ResponseEntity.noContent().build();
    }
}