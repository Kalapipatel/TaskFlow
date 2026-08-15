package com.TaskFlow.ProjectService.service;

import com.TaskFlow.ProjectService.dto.CreateProjectRequest;
import com.TaskFlow.ProjectService.dto.ProjectResponse;
import com.TaskFlow.ProjectService.entity.Project;
import com.TaskFlow.ProjectService.entity.ProjectMember;
import com.TaskFlow.ProjectService.enums.ProjectMemberRole;
import com.TaskFlow.ProjectService.enums.ProjectStatus;
import com.TaskFlow.ProjectService.repository.ProjectMemberRepository;
import com.TaskFlow.ProjectService.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public ProjectResponse createProject(UUID ownerId, CreateProjectRequest createProjectRequest) {
        Project project = new Project();

        project.setName(createProjectRequest.getName());
        project.setDescription(createProjectRequest.getDescription());
        project.setOwnerId(ownerId);
        project.setProjectKey(createProjectRequest.getProjectKey());
        project.setStatus(ProjectStatus.ACTIVE);

        project = projectRepository.save(project);

        ProjectMember owner = new ProjectMember();
        owner.setProject(project);
        owner.setUserId(ownerId);
        owner.setRole(ProjectMemberRole.OWNER);

        projectMemberRepository.save(owner);

        return new ProjectResponse(project.getId(), project.getName(), project.getOwnerId(), project.getProjectKey());
    }
}
