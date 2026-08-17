package com.TaskFlow.ProjectService.repository;

import com.TaskFlow.ProjectService.dto.ProjectResponse;
import com.TaskFlow.ProjectService.entity.ProjectMember;
import com.TaskFlow.ProjectService.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    List<ProjectMember> findAllByProjectId(UUID projectId);

    // Optimized join query fixed to match your specific DTO and constructor parameters
    @Query("SELECT new com.TaskFlow.ProjectService.dto.ProjectResponse(p.id, p.name, p.ownerId, p.projectKey) " +
            "FROM Project p JOIN ProjectMember pm ON p.id = pm.project.id " +
            "WHERE pm.userId = :userId AND p.status = :status ORDER BY p.createdAt DESC")
    List<ProjectResponse> findProjectsByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") ProjectStatus status);

}