package com.learnflow.learnflowserver.dto.request;

import com.learnflow.learnflowserver.dto.PhaseRequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignmentCreateRequest {
    @NotBlank(message = "과목은 필수 항목입니다.")
    private String subject;

    @NotBlank(message = "단원은 필수 항목입니다.")
    private String chapter;

    @NotBlank(message = "주제는 필수 항목입니다.")
    private String topic;

    private String description;

    @NotEmpty(message = "학생 목록은 필수 항목입니다.")
    private List<Long> studentIds;

    @NotEmpty(message = "단계 정보는 필수 항목입니다.")
    private List<PhaseRequestDto> phases;
}