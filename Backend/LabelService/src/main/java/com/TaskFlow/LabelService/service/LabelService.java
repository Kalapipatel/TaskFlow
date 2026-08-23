package com.TaskFlow.LabelService.service;

import com.TaskFlow.LabelService.dto.CreateLabelRequest;
import com.TaskFlow.LabelService.dto.LabelResponse;
import com.TaskFlow.LabelService.dto.UpdateLabelRequest;
import com.TaskFlow.LabelService.entity.Label;
import com.TaskFlow.LabelService.exception.InvalidLabelException;
import com.TaskFlow.LabelService.exception.LabelNameAlreadyExistsException;
import com.TaskFlow.LabelService.exception.LabelNotFoundException;
import com.TaskFlow.LabelService.mapper.LabelMapper;
import com.TaskFlow.LabelService.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;

    /*
     * Operation 1: Create a label.
     *
     * Phase 2 isolation deliberately omits Project Service membership
     * verification. That check belongs to Phase 3 Feign wiring.
     */
    @Transactional
    public LabelResponse createLabel(
            UUID creatorId,
            CreateLabelRequest request
    ) {
        String normalizedName = request.name().strip();
        String normalizedColor = request.color().toUpperCase(Locale.ROOT);

        if (labelRepository.existsByProjectIdAndName(
                request.projectId(),
                normalizedName
        )) {
            throw new LabelNameAlreadyExistsException(normalizedName);
        }

        Label label = labelMapper.toEntity(
                normalizedName,
                normalizedColor,
                request.projectId(),
                creatorId
        );

        Label savedLabel = labelRepository.save(label);
        return labelMapper.toResponse(savedLabel);
    }

    /*
     * Operation 2: List labels for a project.
     *
     * Phase 3 will add requester membership validation before this query.
     * Pagination is intentionally omitted because the documented expected
     * project label count is small.
     */
    @Transactional(readOnly = true)
    public List<LabelResponse> getLabelsByProject(UUID projectId) {
        return labelRepository
                .findAllByProjectIdOrderByNameAsc(projectId)
                .stream()
                .map(labelMapper::toResponse)
                .toList();
    }

    /*
     * Operation 3: Update a label.
     *
     * Phase 2 isolation deliberately omits the Project Service role lookup.
     * requesterId remains part of the service contract so Phase 3 can add the
     * MEMBER/ADMIN/OWNER authorization rule without changing the endpoint.
     */
    @Transactional
    public LabelResponse updateLabel(
            UUID requesterId,
            UUID labelId,
            UpdateLabelRequest request
    ) {
        Label label = getLabel(labelId);

        if (request.name() != null) {
            String normalizedName = request.name().strip();

            if (!normalizedName.equals(label.getName())
                    && labelRepository.existsByProjectIdAndNameAndIdNot(
                    label.getProjectId(),
                    normalizedName,
                    label.getId()
            )) {
                throw new LabelNameAlreadyExistsException(normalizedName);
            }

            label.setName(normalizedName);
        }

        if (request.color() != null) {
            label.setColor(request.color().toUpperCase(Locale.ROOT));
        }

        Label updatedLabel = labelRepository.save(label);
        return labelMapper.toResponse(updatedLabel);
    }

    /*
     * Operation 4: Delete a label.
     *
     * Phase 2 isolation deletes only from labels_db. The ADMIN/OWNER check and
     * label-service.label.deleted Kafka event belong to later build phases.
     */
    @Transactional
    public void deleteLabel(UUID requesterId, UUID labelId) {
        Label label = getLabel(labelId);
        labelRepository.delete(label);
    }

    /*
     * Operation 5: Get one label by ID.
     *
     * This is a read-only public lookup. Phase 3 security wiring can add the
     * documented project-membership check without changing this data access.
     */
    @Transactional(readOnly = true)
    public LabelResponse getLabelById(UUID labelId) {
        return labelMapper.toResponse(getLabel(labelId));
    }

    /*
     * Internal operation used by Task Service before attaching labels.
     * Missing IDs and labels owned by another project are intentionally treated
     * the same so callers cannot attach cross-project labels.
     */
    @Transactional(readOnly = true)
    public List<LabelResponse> validateLabels(
            UUID projectId,
            List<UUID> labelIds
    ) {
        Set<UUID> requestedLabelIds = new LinkedHashSet<>(labelIds);

        if (requestedLabelIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Label> labelsById = indexById(
                labelRepository.findAllByIdInAndProjectId(
                        requestedLabelIds,
                        projectId
                )
        );

        List<UUID> invalidLabelIds = requestedLabelIds.stream()
                .filter(labelId -> !labelsById.containsKey(labelId))
                .toList();

        if (!invalidLabelIds.isEmpty()) {
            throw new InvalidLabelException(projectId, invalidLabelIds);
        }

        return mapInRequestOrder(requestedLabelIds, labelsById);
    }

    /*
     * Internal enrichment operation used by Task Service. Missing labels are
     * omitted to support graceful reads during asynchronous deletion cleanup.
     */
    @Transactional(readOnly = true)
    public List<LabelResponse> getLabelsBatch(List<UUID> labelIds) {
        Set<UUID> requestedLabelIds = new LinkedHashSet<>(labelIds);

        if (requestedLabelIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, Label> labelsById = indexById(
                labelRepository.findAllById(requestedLabelIds)
        );

        return mapInRequestOrder(requestedLabelIds, labelsById);
    }

    private Label getLabel(UUID labelId) {
        return labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException(labelId));
    }

    private Map<UUID, Label> indexById(List<Label> labels) {
        return labels.stream()
                .collect(Collectors.toMap(
                        Label::getId,
                        Function.identity(),
                        (existing, ignored) -> existing,
                        LinkedHashMap::new
                ));
    }

    private List<LabelResponse> mapInRequestOrder(
            Set<UUID> requestedLabelIds,
            Map<UUID, Label> labelsById
    ) {
        return requestedLabelIds.stream()
                .map(labelsById::get)
                .filter(java.util.Objects::nonNull)
                .map(labelMapper::toResponse)
                .toList();
    }
}
