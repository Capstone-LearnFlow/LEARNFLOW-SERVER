package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.common.enums.CreatedBy;
import com.learnflow.learnflowserver.domain.common.enums.NodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeTreeResponse {
    private Long id;
    private String content;
    private String summary;
    private NodeType type;
    private CreatedBy createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<EvidenceResponse> evidences;
    private List<NodeTreeResponse> children;
    private Long triggeredByEvidenceId;
}