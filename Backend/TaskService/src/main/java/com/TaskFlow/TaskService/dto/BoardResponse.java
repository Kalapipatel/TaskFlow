package com.TaskFlow.TaskService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {
    @JsonProperty("TODO")
    private List<TaskSummary> todo;

    @JsonProperty("IN_PROGRESS")
    private List<TaskSummary> inProgress;

    @JsonProperty("IN_REVIEW")
    private List<TaskSummary> inReview;

    @JsonProperty("DONE")
    private List<TaskSummary> done;
}