package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.common.enums.CreatedBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ActivityLogResponse {
    private LocalDateTime timestamp;
    private CreatedBy actionBy;
    private NodeLogInfo node;
    private List<EvidenceLogInfo> evidences;
}