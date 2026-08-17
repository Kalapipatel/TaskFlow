package com.TaskFlow.ProjectService.service;

import com.TaskFlow.ProjectService.dto.*;
import com.TaskFlow.ProjectService.entity.Project;
import com.TaskFlow.ProjectService.entity.ProjectMember;
import com.TaskFlow.ProjectService.enums.ProjectMemberRole;
import com.TaskFlow.ProjectService.enums.ProjectStatus;
import com.TaskFlow.ProjectService.exception.*;
import com.TaskFlow.ProjectService.repository.ProjectMemberRepository;
import com.TaskFlow.ProjectService.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // --- RBAC HELPER METHODS ---

    private Project getActiveProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new ProjectArchivedOperationException("Cannot modify an archived project");
        }
        return project;
    }

    private ProjectMember requireRole(UUID projectId, UUID userId, ProjectMemberRole... requiredRoles) {
        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotProjectMemberException("You are not a member of this project"));

        boolean hasRole = Arrays.asList(requiredRoles).contains(membership.getRole());
        if (!hasRole) {
            throw new InsufficientRoleException("This action requires " + Arrays.toString(requiredRoles) + " role");
        }
        return membership;
    }

    // --- CORE PROJECT LOGIC ---

    @Transactional
    public ProjectResponse createProject(UUID ownerId, CreateProjectRequest request) {
        if (projectRepository.existsByProjectKey(request.getProjectKey())) {
            throw new ProjectKeyAlreadyExistsException(request.getProjectKey());
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwnerId(ownerId);
        project.setProjectKey(request.getProjectKey());
        project.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);

        ProjectMember owner = new ProjectMember();
        owner.setProject(project);
        owner.setUserId(ownerId);
        owner.setRole(ProjectMemberRole.OWNER); // Auto-add creator as OWNER
        projectMemberRepository.save(owner);

        return new ProjectResponse(project.getId(), project.getName(), project.getOwnerId(), project.getProjectKey());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(UUID userId) {
        return projectMemberRepository.findProjectsByUserIdAndStatus(userId, ProjectStatus.ACTIVE);
    }

    @Transactional
    public ProjectResponse updateProject(UUID requesterId, UUID projectId, UpdateProjectRequest req) {
        Project project = getActiveProject(projectId);
        requireRole(projectId, requesterId, ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);

        if (req.getName() != null) project.setName(req.getName());
        if (req.getDescription() != null) project.setDescription(req.getDescription());

        project = projectRepository.save(project);
        return new ProjectResponse(project.getId(), project.getName(), project.getOwnerId(), project.getProjectKey());
    }

    @Transactional
    public void archiveProject(UUID requesterId, UUID projectId) {
        Project project = getActiveProject(projectId);
        requireRole(projectId, requesterId, ProjectMemberRole.OWNER);

        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
    }

    // --- MEMBERSHIP LOGIC ---

    @Transactional
    public ProjectMemberResponse addMember(UUID requesterId, UUID projectId, AddMemberRequest req) {
        getActiveProject(projectId);
        requireRole(projectId, requesterId, ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);

        if (req.getRole() == ProjectMemberRole.OWNER) {
            throw new InsufficientRoleException("Cannot assign OWNER role directly.");
        }

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, req.getUserId())) {
            throw new ApiException(HttpStatus.CONFLICT, "User is already a member");
        }

        // MOCK USER for Phase 2 standalone testing[cite: 1]
        String mockFullName = "Mock User";
        String mockEmail = "mock@example.com";

        ProjectMember newMember = new ProjectMember();
        newMember.setProject(projectRepository.getReferenceById(projectId));
        newMember.setUserId(req.getUserId());
        newMember.setRole(req.getRole());
        newMember = projectMemberRepository.save(newMember);

        return new ProjectMemberResponse(
                req.getUserId(),
                mockFullName,
                mockEmail,
                newMember.getRole(),
                newMember.getJoinedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getMembers(UUID requesterId, UUID projectId) {
        getActiveProject(projectId);
        requireRole(projectId, requesterId, ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN, ProjectMemberRole.MEMBER, ProjectMemberRole.VIEWER);

        List<ProjectMember> members = projectMemberRepository.findAllByProjectId(projectId);

        // MOCK USER mapping for Phase 2 standalone testing[cite: 1]
        return members.stream().map(m -> new ProjectMemberResponse(
                m.getUserId(),
                "Mock User",
                "mock@example.com",
                m.getRole(),
                m.getJoinedAt()
        )).collect(Collectors.toList());
    }

    @Transactional
    public void changeRole(UUID requesterId, UUID projectId, UUID targetUserId, ChangeRoleRequest req) {
        getActiveProject(projectId);
        requireRole(projectId, requesterId, ProjectMemberRole.OWNER);

        ProjectMember targetMember = projectMemberRepository.findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found in project"));

        if (targetMember.getRole() == ProjectMemberRole.OWNER || req.getRole() == ProjectMemberRole.OWNER) {
            throw new InsufficientRoleException("Cannot modify OWNER role directly.");
        }

        targetMember.setRole(req.getRole());
        projectMemberRepository.save(targetMember);
    }

    @Transactional
    public void removeMember(UUID requesterId, UUID projectId, UUID targetUserId) {
        getActiveProject(projectId);
        requireRole(projectId, requesterId, ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);

        ProjectMember targetMember = projectMemberRepository.findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found in project"));

        if (targetMember.getRole() == ProjectMemberRole.OWNER) {
            throw new CannotRemoveOwnerException("Cannot remove project owner.");
        }

        projectMemberRepository.delete(targetMember);
    }

    // --- INTERNAL GATEKEEPER API ---

    @Transactional(readOnly = true)
    public ProjectResponse getProjectInternal(UUID projectId) {
        Project project = getActiveProject(projectId);
        return new ProjectResponse(project.getId(), project.getName(), project.getOwnerId(), project.getProjectKey());
    }

    @Transactional(readOnly = true)
    public MembershipResponse verifyMembership(UUID projectId, UUID userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NotProjectMemberException("User is not a member of project " + projectId));
        return new MembershipResponse(projectId, userId, member.getRole());
    }
}






//package com.TaskFlow.ProjectService.service;
//
//import com.TaskFlow.ProjectService.client.UserServiceClient;
//import com.TaskFlow.ProjectService.dto.*;
//import com.TaskFlow.ProjectService.entity.Project;
//import com.TaskFlow.ProjectService.entity.ProjectMember;
//import com.TaskFlow.ProjectService.enums.ProjectMemberRole;
//import com.TaskFlow.ProjectService.enums.ProjectStatus;
//import com.TaskFlow.ProjectService.exception.*;
//import com.TaskFlow.ProjectService.repository.ProjectMemberRepository;
//import com.TaskFlow.ProjectService.repository.ProjectRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ProjectService {
//
//    private final ProjectRepository projectRepository;
//    private final ProjectMemberRepository projectMemberRepository;
//    private final UserServiceClient userServiceClient;
//
//    // --- RBAC HELPER METHODS[cite: 2] ---
//
//    private Project getActiveProject(UUID projectId) {
//        Project project = projectRepository.findById(projectId)
//                .orElseThrow(() -> new ProjectNotFoundException(projectId));
//
//        if (project.getStatus() == ProjectStatus.ARCHIVED) {
//            throw new ProjectArchivedOperationException("Cannot modify an archived project");
//        }
//        return project;
//    }
//
//    private ProjectMember requireRole(UUID projectId, UUID userId, ProjectMemberRole... requiredRoles) {
//        ProjectMember membership = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
//                .orElseThrow(() -> new NotProjectMemberException("You are not a member of this project"));
//
//        boolean hasRole = Arrays.asList(requiredRoles).contains(membership.getRole());
//        if (!hasRole) {
//            throw new InsufficientRoleException("This action requires " + Arrays.toString(requiredRoles) + " role");
//        }
//        return membership;
//    }
//
//    // --- CORE PROJECT LOGIC ---
//
//    @Transactional
//    public ProjectResponse createProject(UUID ownerId, CreateProjectRequest request) {
//        if (projectRepository.existsByProjectKey(request.getProjectKey())) {
//            throw new ProjectKeyAlreadyExistsException(request.getProjectKey());
//        }
//
//        Project project = new Project();
//        project.setName(request.getName());
//        project.setDescription(request.getDescription());
//        project.setOwnerId(ownerId);
//        project.setProjectKey(request.getProjectKey());
//        project.setStatus(ProjectStatus.ACTIVE);
//        project = projectRepository.save(project);
//
//        ProjectMember owner = new ProjectMember();
//        owner.setProject(project);
//        owner.setUserId(ownerId);
//        owner.setRole(ProjectMemberRole.OWNER); // Auto-add creator as OWNER[cite: 2]
//        projectMemberRepository.save(owner);
//
//        // TODO: Publish Kafka event 'project.created' here[cite: 2]
//
//        return new ProjectResponse(project.getId(), project.getName(), project.getOwnerId(), project.getProjectKey());
//    }
//
//    @Transactional(readOnly = true)
//    public List<ProjectResponse> getMyProjects(UUID userId) {
//        // Calling your updated repository method directly
//        return projectMemberRepository.findProjectsByUserIdAndStatus(userId, ProjectStatus.ACTIVE);
//    }
//
//    @Transactional
//    public ProjectResponse updateProject(UUID requesterId, UUID projectId, UpdateProjectRequest req) {
//        Project project = getActiveProject(projectId);
//        requireRole(projectId, requesterId, ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);
//
//        if (req.getName() != null) project.setName(req.getName());
//        if (req.getDescription() != null) project.setDescription(req.getDescription());
//
//        project = projectRepository.save(project);
//        return new ProjectResponse(project.getId(), project.getName(), project.getOwnerId(), project.getProjectKey());
//    }
//
//    @Transactional
//    public void archiveProject(UUID requesterId, UUID projectId) {
//        Project project = getActiveProject(projectId);
//        requireRole(projectId, requesterId, ProjectMemberRole.OWNER); // Only OWNER can archive[cite: 2]
//
//        project.setStatus(ProjectStatus.ARCHIVED);
//        projectRepository.save(project);
//    }
//
//    // --- MEMBERSHIP LOGIC ---
//
//    @Transactional
//    public ProjectMemberResponse addMember(UUID requesterId, UUID projectId, AddMemberRequest req) {
//        getActiveProject(projectId);
//        requireRole(projectId, requesterId, ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);
//
//        if (req.getRole() == ProjectMemberRole.OWNER) {
//            throw new InsufficientRoleException("Cannot assign OWNER role directly.");
//        }
//
//        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, req.getUserId())) {
//            throw new ApiException(HttpStatus.CONFLICT, "User is already a member");
//        }
//
//        // Cross-service call to verify user exists BEFORE adding them to the project[cite: 2]
//        UserServiceClient.UserResponse user = userServiceClient.getUser(req.getUserId());
//
//        ProjectMember newMember = new ProjectMember();
//        newMember.setProject(projectRepository.getReferenceById(projectId));
//        newMember.setUserId(req.getUserId());
//        newMember.setRole(req.getRole());
//        newMember = projectMemberRepository.save(newMember);
//
//        return new ProjectMemberResponse(
//                user.getId(),
//                user.getFullName(),
//                user.getEmail(),
//                newMember.getRole(),
//                newMember.getJoinedAt()
//        );
//    }
//
//    @Transactional(readOnly = true)
//    public List<ProjectMemberResponse> getMembers(UUID requesterId, UUID projectId) {
//        getActiveProject(projectId);
//        requireRole(projectId, requesterId, ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN, ProjectMemberRole.MEMBER, ProjectMemberRole.VIEWER);
//
//        List<ProjectMember> members = projectMemberRepository.findAllByProjectId(projectId);
//
//        // Note: For SDE-1 scope, individual Feign mapping is used here.
//        // For production, this should be a batch Feign call[cite: 2].
//        return members.stream().map(m -> {
//            UserServiceClient.UserResponse user = userServiceClient.getUser(m.getUserId());
//            return new ProjectMemberResponse(
//                    user.getId(),
//                    user.getFullName(),
//                    user.getEmail(),
//                    m.getRole(),
//                    m.getJoinedAt()
//            );
//        }).collect(Collectors.toList());
//    }
//
//    @Transactional
//    public void changeRole(UUID requesterId, UUID projectId, UUID targetUserId, ChangeRoleRequest req) {
//        getActiveProject(projectId);
//        requireRole(projectId, requesterId, ProjectMemberRole.OWNER); // Only OWNER can change roles[cite: 2]
//
//        ProjectMember targetMember = projectMemberRepository.findByProjectIdAndUserId(projectId, targetUserId)
//                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found in project"));
//
//        if (targetMember.getRole() == ProjectMemberRole.OWNER || req.getRole() == ProjectMemberRole.OWNER) {
//            throw new InsufficientRoleException("Cannot modify OWNER role directly.");
//        }
//
//        targetMember.setRole(req.getRole());
//        projectMemberRepository.save(targetMember);
//    }
//
//    @Transactional
//    public void removeMember(UUID requesterId, UUID projectId, UUID targetUserId) {
//        getActiveProject(projectId);
//        requireRole(projectId, requesterId, ProjectMemberRole.OWNER, ProjectMemberRole.ADMIN);
//
//        ProjectMember targetMember = projectMemberRepository.findByProjectIdAndUserId(projectId, targetUserId)
//                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member not found in project"));
//
//        if (targetMember.getRole() == ProjectMemberRole.OWNER) {
//            throw new CannotRemoveOwnerException("Cannot remove project owner."); // Hard constraint[cite: 2]
//        }
//
//        projectMemberRepository.delete(targetMember);
//    }
//
//    // --- INTERNAL GATEKEEPER API ---
//
//    @Transactional(readOnly = true)
//    public ProjectResponse getProjectInternal(UUID projectId) {
//        Project project = getActiveProject(projectId);
//        return new ProjectResponse(project.getId(), project.getName(), project.getOwnerId(), project.getProjectKey());
//    }
//
//    @Transactional(readOnly = true)
//    public MembershipResponse verifyMembership(UUID projectId, UUID userId) {
//        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
//                .orElseThrow(() -> new NotProjectMemberException("User is not a member of project " + projectId));
//        return new MembershipResponse(projectId, userId, member.getRole());
//    }
//}