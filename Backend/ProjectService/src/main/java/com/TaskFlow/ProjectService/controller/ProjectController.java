package com.TaskFlow.ProjectService.controller;

import com.TaskFlow.ProjectService.dto.CreateProjectRequest;
import com.TaskFlow.ProjectService.dto.ProjectResponse;
import com.TaskFlow.ProjectService.security.UserPrincipal;
import com.TaskFlow.ProjectService.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/projects")
    public ResponseEntity<ProjectResponse> createProject(@AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateProjectRequest createProjectRequest){

        UUID ownerId = principal.userId();

        ProjectResponse projectResponse = projectService.createProject(ownerId, createProjectRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponse);
    }

}
