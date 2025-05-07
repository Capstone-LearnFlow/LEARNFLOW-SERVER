package com.learnflow.learnflowserver.dto;

import com.learnflow.learnflowserver.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class StudentDto {
    private Long id;
    private String number;
    private String name;

    public static StudentDto from(User user) {
        return StudentDto.builder()
                .id(user.getId())
                .number(user.getNumber())
                .name(user.getName())
                .build();
    }
}