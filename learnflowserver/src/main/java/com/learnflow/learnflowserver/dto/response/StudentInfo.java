package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StudentInfo {
    private Long id;
    private String name;
    private String number;

    public static StudentInfo from(User student) {
        return StudentInfo.builder()
                .id(student.getId())
                .name(student.getName())
                .number(student.getNumber())
                .build();
    }
}