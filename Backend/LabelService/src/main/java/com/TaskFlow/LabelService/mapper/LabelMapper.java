package com.TaskFlow.LabelService.mapper;

import com.TaskFlow.LabelService.dto.LabelResponse;
import com.TaskFlow.LabelService.entity.Label;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LabelMapper {

    public Label toEntity(
            String normalizedName,
            String normalizedColor,
            UUID projectId,
            UUID createdBy
    ) {
        Label label = new Label();
        label.setName(normalizedName);
        label.setColor(normalizedColor);
        label.setProjectId(projectId);
        label.setCreatedBy(createdBy);
        return label;
    }

    public LabelResponse toResponse(Label label) {
        return new LabelResponse(
                label.getId(),
                label.getName(),
                label.getColor(),
                label.getProjectId(),
                label.getCreatedBy(),
                label.getCreatedAt(),
                label.getUpdatedAt()
        );
    }
}