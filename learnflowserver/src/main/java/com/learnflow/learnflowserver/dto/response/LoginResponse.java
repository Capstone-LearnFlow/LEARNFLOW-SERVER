package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Long id;
    private String name;
    private Role role;

    public static LoginResponse from(Long id, String name, Role role) {
        return LoginResponse.builder()
                .id(id)
                .name(name)
                .role(role)
                .build();
    }
}
