package com.learnflow.learnflowserver.dto.response;

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
public class NodeTreeLogResponse {
    private Long id;
    private String content;
    private String summary;
    private NodeType type;
    private CreatedBy createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<EvidenceLogResponse> evidences;
    private List<NodeTreeLogResponse> children;
    private Long triggeredByEvidenceId;
    private boolean isHidden; // 숨김 상태 추가
}