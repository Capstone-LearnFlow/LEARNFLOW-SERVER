package com.learnflow.learnflowserver.dto.request;

import com.learnflow.learnflowserver.domain.common.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponseRequest {
    private TargetType targetType; // NODE 또는 EVIDENCE
    private Long targetId; // question_node_id 또는 evidence_id
    private String content;
    private List<EvidenceCreateRequest> evidences; // targetType이 EVIDENCE일 때만 사용
}