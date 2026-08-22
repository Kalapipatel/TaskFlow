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

    // Step 2: Fetch top-level comments only (parent_comment_id IS NULL) and ensure they aren't deleted
    @Query("SELECT c FROM Comment c WHERE c.taskId = :taskId AND c.parentComment IS NULL AND c.isDeleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findTopLevelCommentsByTaskId(@Param("taskId") UUID taskId, Pageable pageable);

    // Step 3: Fetch all replies for the returned top-level comment IDs in a single query[cite: 3]
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id IN :parentIds ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentIds(@Param("parentIds") List<UUID> parentIds);

    // Check if a comment has replies to determine soft vs. hard delete
    long countByParentCommentId(UUID parentCommentId);


    // Fetch comment counts grouped by task ID, ignoring soft-deleted comments
    @Query("SELECT c.taskId AS taskId, COUNT(c) AS commentCount FROM Comment c WHERE c.taskId IN :taskIds AND c.isDeleted = false GROUP BY c.taskId")
    List<CommentCountProjection> countCommentsByTaskIds(@Param("taskIds") List<UUID> taskIds);

    // Hard delete all comments for a specific task
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.taskId = :taskId")
    void deleteAllByTaskId(@Param("taskId") UUID taskId);

//    // Fetches only top-level comments (parent is null) that are not deleted, for pagination
//    Page<Comment> findByTaskIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtAsc(UUID taskId, Pageable pageable);
//
//    // Fetches all replies for a batch of top-level comments in a single query
//    List<Comment> findByParentCommentIdInOrderByCreatedAtAsc(List<UUID> parentCommentIds);
//
//    // Used by the internal endpoint to aggregate comment counts for the Kanban board
//    @Query("SELECT c.taskId, COUNT(c) FROM Comment c WHERE c.taskId IN :taskIds AND c.isDeleted = false GROUP BY c.taskId")
//    List<Object[]> countCommentsByTaskIds(@Param("taskIds") List<UUID> taskIds);
//
//    // Used by the Kafka consumer to clean up comments when a task is hard-deleted
//    @Modifying
//    @Query("DELETE FROM Comment c WHERE c.taskId = :taskId")
//    void deleteByTaskId(@Param("taskId") UUID taskId);
}
