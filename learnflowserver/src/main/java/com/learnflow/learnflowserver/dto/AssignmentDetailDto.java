package com.learnflow.learnflowserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AssignmentDetailDto {
    private Long id;
    private String subject;
    private String chapter;
    private String topic;
    private String description;
    private String teacherName;
    private List<PhaseInfoDto> phases;
}