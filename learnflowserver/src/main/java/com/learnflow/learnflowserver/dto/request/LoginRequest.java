package com.learnflow.learnflowserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "학번 또는 아이디는 필수 항목입니다.")
    private String number;
}
