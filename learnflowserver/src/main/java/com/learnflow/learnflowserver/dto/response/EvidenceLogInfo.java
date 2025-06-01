package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.Evidence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EvidenceLogInfo {
    private Long id;
    private String content;
    private String summary;
    private String source;

    public static EvidenceLogInfo from(Evidence evidence) {
        return EvidenceLogInfo.builder()
                .id(evidence.getId())
                .content(evidence.getContent())
                .summary(evidence.getSummary())
                .source(evidence.getSource())
                .build();
    }
}