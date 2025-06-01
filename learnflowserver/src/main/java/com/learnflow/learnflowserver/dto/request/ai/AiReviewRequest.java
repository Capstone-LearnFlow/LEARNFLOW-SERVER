package com.learnflow.learnflowserver.dto.request.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.learnflow.learnflowserver.dto.request.TreeNodeRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiReviewRequest {
    private TreeNodeRequest tree;

    @JsonProperty("review_num")
    private Integer reviewNum = 1; // 기본값 1

    @JsonProperty("student_id")
    private String studentId;

    @JsonProperty("assignment_id")
    private String assignmentId;
}