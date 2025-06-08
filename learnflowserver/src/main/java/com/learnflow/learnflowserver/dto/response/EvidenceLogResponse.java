package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.Evidence;
import com.learnflow.learnflowserver.domain.common.enums.CreatedBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class EvidenceLogResponse {
    private Long id;
    private String content;
    private String summary;
    private String source;
    private String url;
    private CreatedBy createdBy;
    private LocalDateTime createdAt;

    public static EvidenceLogResponse from(Evidence evidence) {
        return EvidenceLogResponse.builder()
                .id(evidence.getId())
                .content(evidence.getContent())
                .summary(evidence.getSummary())
                .source(evidence.getSource())
                .url(evidence.getUrl())
                .createdBy(evidence.getCreatedBy())
                .createdAt(evidence.getCreatedAt())
                .build();
    }
}