package com.learnflow.learnflowserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AssignmentSummaryForTeacherDto {
    private Long id;
    private String subject;
    private String chapter;
    private String topic;
    private LocalDateTime createdAt;
    private Integer studentCount;
    private Integer currentPhase;
}