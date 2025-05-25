package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.Node;
import com.learnflow.learnflowserver.domain.common.enums.CreatedBy;
import com.learnflow.learnflowserver.domain.common.enums.NodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class NodeDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private NodeType type;
    private CreatedBy createdBy;
    private Long parentId;
    private List<EvidenceResponse> evidences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NodeDetailResponse of(Node node, String title, List<EvidenceResponse> evidences) {
        return NodeDetailResponse.builder()
                .id(node.getId())
                .title(title)
                .content(node.getContent())
                .summary(node.getSummary())
                .type(node.getType())
                .createdBy(node.getCreatedBy())
                .parentId(node.getParent() != null ? node.getParent().getId() : null)
                .evidences(evidences)
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .build();
    }
}