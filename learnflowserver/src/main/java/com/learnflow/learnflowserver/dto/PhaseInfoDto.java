package com.learnflow.learnflowserver.dto;

import com.learnflow.learnflowserver.domain.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PhaseInfoDto {
    private Integer phaseNumber;
    private Status status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}