package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.common.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class StudentActivitySummary {
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private int nodeCount;
    private int evidenceCount;
    private int aiInteractions;
    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;
    private StudentStatus status;
}