package com.TaskFlow.TaskService.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachLabelsRequest {

    @NotNull(message = "labelIds set cannot be null")
    @NotEmpty(message = "labelIds set cannot be empty")
    private Set<UUID> labelIds;
}