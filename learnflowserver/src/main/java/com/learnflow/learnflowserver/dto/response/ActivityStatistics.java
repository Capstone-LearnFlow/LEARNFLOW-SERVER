package com.learnflow.learnflowserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ActivityStatistics {
    private int totalNodes;
    private int studentNodes;
    private int aiNodes;
    private int aiInteractions; // AI 노드 수와 동일
    private int totalEvidences;
    private int studentEvidences;
    private int aiEvidences;
    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;
    private String totalDuration;
}