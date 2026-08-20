package com.TaskFlow.CommentService.repository;

import com.TaskFlow.CommentService.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Fetches only top-level comments (parent is null) that are not deleted, for pagination
    Page<Comment> findByTaskIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(UUID taskId, Pageable pageable);

    // Fetches all replies for a batch of top-level comments in a single query
    List<Comment> findByParentCommentIdInOrderByCreatedAtAsc(List<UUID> parentCommentIds);

    // Used by the internal endpoint to aggregate comment counts for the Kanban board
    @Query("SELECT c.taskId, COUNT(c) FROM Comment c WHERE c.taskId IN :taskIds AND c.isDeleted = false GROUP BY c.taskId")
    List<Object[]> countCommentsByTaskIds(@Param("taskIds") List<UUID> taskIds);

    // Used by the Kafka consumer to clean up comments when a task is hard-deleted
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.taskId = :taskId")
    void deleteByTaskId(@Param("taskId") UUID taskId);
}
