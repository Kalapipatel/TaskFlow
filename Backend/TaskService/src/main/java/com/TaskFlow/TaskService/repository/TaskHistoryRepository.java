package com.TaskFlow.TaskService.repository;

import com.TaskFlow.TaskService.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, UUID> {

    // Uses idx_th_task_id to fetch the audit log for a specific task, sorted newest to oldest
    List<TaskHistory> findByTaskIdOrderByChangedAtDesc(UUID taskId);
}
