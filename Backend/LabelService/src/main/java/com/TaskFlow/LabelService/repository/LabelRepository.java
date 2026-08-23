package com.TaskFlow.LabelService.repository;

import com.TaskFlow.LabelService.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<Label, UUID> {

    /*
     * The MySQL name column uses a case-insensitive collation, so this equality
     * check is already case-insensitive and can use uk_labels_project_name.
     * Applying UPPER(name) would make index usage less efficient.
     */
    boolean existsByProjectIdAndName(UUID projectId, String name);

    /*
     * uk_labels_project_name starts with (project_id, name), supporting both
     * project filtering and alphabetical ordering.
     */
    List<Label> findAllByProjectIdOrderByNameAsc(UUID projectId);

    /*
     * Used when renaming a label. Excluding the current label allows display-only
     * case changes while still rejecting a conflicting label in the same project.
     */
    boolean existsByProjectIdAndNameAndIdNot(UUID projectId, String name, UUID labelId);

    /*
     * Restricts validation to the owning project in one query. MySQL can use
     * the UUID primary key for the ID lookup; no additional index is needed.
     */
    List<Label> findAllByIdInAndProjectId(
            Collection<UUID> labelIds,
            UUID projectId
    );
}
