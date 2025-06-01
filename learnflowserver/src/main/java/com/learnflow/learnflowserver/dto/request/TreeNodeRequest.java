package com.learnflow.learnflowserver.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TreeNodeRequest {
    private String id;
    private String type; // "주제" | "주장" | "근거" | "반론" | "질문" | "답변"
    private List<TreeNodeRequest> child;
    private List<TreeNodeRequest> sibling;
    private String content;
    private String summary;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}