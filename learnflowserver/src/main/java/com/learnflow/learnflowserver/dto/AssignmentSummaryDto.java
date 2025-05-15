package com.learnflow.learnflowserver.dto;

import com.learnflow.learnflowserver.domain.common.enums.Status;
import com.learnflow.learnflowserver.domain.common.enums.StudentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AssignmentSummaryDto {
    private Long id;
    private String subject;
    private String chapter;
    private String topic;
    private Status assignmentStatus;
    private String teacherName;
    private Integer currentPhase;
    private StudentStatus studentStatus;
    private LocalDateTime phaseEndDate;
}