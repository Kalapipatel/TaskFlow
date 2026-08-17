package com.TaskFlow.ProjectService.repository;

import com.TaskFlow.ProjectService.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    boolean existsByProjectKey(String projectKey);

}
