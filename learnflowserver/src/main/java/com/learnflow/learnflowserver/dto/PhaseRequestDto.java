package com.learnflow.learnflowserver.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class PhaseRequestDto {
    @NotNull(message = "단계 번호는 필수 항목입니다.")
    private Integer phaseNumber;

    @NotNull(message = "시작일은 필수 항목입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수 항목입니다.")
    private LocalDateTime endDate;
}