package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.Node;
import com.learnflow.learnflowserver.domain.common.enums.CreatedBy;
import com.learnflow.learnflowserver.domain.common.enums.NodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class NodeResponse {
    private Long nodeId;
    private String title;
    private String content;
    private String summary;
    private NodeType type;
    private CreatedBy createdBy;
    private List<EvidenceResponse> evidences;
    private LocalDateTime createdAt;

    public static NodeResponse of(Node node, String title, List<EvidenceResponse> evidences) {
        return NodeResponse.builder()
                .nodeId(node.getId())
                .title(title)
                .content(node.getContent())
                .summary(node.getSummary())
                .type(node.getType())
                .createdBy(node.getCreatedBy())
                .evidences(evidences)
                .createdAt(node.getCreatedAt())
                .build();
    }
}