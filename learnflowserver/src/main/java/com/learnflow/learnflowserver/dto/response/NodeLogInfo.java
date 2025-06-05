package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.Node;
import com.learnflow.learnflowserver.domain.common.enums.NodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class NodeLogInfo {
    private Long id;
    private NodeType type;
    private String content;
    private String summary;
    private Long parentId;
    private Long triggeredByEvidenceId;
    private boolean isHidden;

    public static NodeLogInfo from(Node node) {
        return NodeLogInfo.builder()
                .id(node.getId())
                .type(node.getType())
                .content(node.getContent())
                .summary(node.getSummary())
                .parentId(node.getParent() != null ? node.getParent().getId() : null)
                .triggeredByEvidenceId(node.getTriggeredByEvidence() != null ?
                        node.getTriggeredByEvidence().getId() : null)
                .isHidden(node.isHidden())
                .build();
    }

    public static NodeLogInfo fromWithStatus(Node node) {
        return from(node);
    }
}