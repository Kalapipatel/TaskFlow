package com.TaskFlow.TaskService.repository;

import com.TaskFlow.TaskService.entity.Task;
import com.TaskFlow.TaskService.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // PERFECT MATCH for our idx_tasks_project_status composite index.
    // Extremely fast for loading specific Kanban columns (e.g., all "TODO" tasks in a project).
    List<Task> findByProjectIdAndStatus(UUID projectId, TaskStatus status);

    // Uses the left-prefix of idx_tasks_project_status to load the whole board quickly.
    List<Task> findByProjectId(UUID projectId);

    // PERFECT MATCH for our uk_tasks_project_number unique constraint.
    // Used when routing URLs like /projects/{projectId}/tasks/{taskNumber} (e.g., PROJ-123)
    Optional<Task> findByProjectIdAndTaskNumber(UUID projectId, Integer taskNumber);

    // Used to find the highest task number currently existing in a project so we can increment for the next one.
    @Query("SELECT MAX(t.taskNumber) FROM Task t WHERE t.projectId = :projectId")
    Optional<Integer> findMaxTaskNumberByProjectId(@Param("projectId") UUID projectId);

    // Uses idx_tasks_assignee_id: "My Tasks" view across all projects
    List<Task> findByAssigneeId(UUID assigneeId);

    // Native query mapping ENUM priorities to numeric weights for custom sorting
    // Relies on idx_tasks_project_status for fast filtering before sorting
    @Query(value = """
            SELECT * FROM tasks 
            WHERE project_id = :projectId 
            AND status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW') 
            ORDER BY 
              CASE priority 
                WHEN 'CRITICAL' THEN 4 
                WHEN 'HIGH' THEN 3 
                WHEN 'MEDIUM' THEN 2 
                WHEN 'LOW' THEN 1 
              END DESC, 
              created_at ASC
            """, nativeQuery = true)
    List<Task> findActiveBoardTasks(@Param("projectId") UUID projectId);

    // Separated DONE query to exclusively support OFFSET/LIMIT pagination
    @Query(value = """
            SELECT * FROM tasks 
            WHERE project_id = :projectId 
            AND status = 'DONE' 
            ORDER BY 
              CASE priority 
                WHEN 'CRITICAL' THEN 4 
                WHEN 'HIGH' THEN 3 
                WHEN 'MEDIUM' THEN 2 
                WHEN 'LOW' THEN 1 
              END DESC, 
              created_at ASC 
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Task> findDoneBoardTasks(
            @Param("projectId") UUID projectId,
            @Param("limit") int limit,
            @Param("offset") int offset);
}