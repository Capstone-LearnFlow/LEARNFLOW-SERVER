package com.learnflow.learnflowserver.dto.response;

import com.learnflow.learnflowserver.domain.Assignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class AssignmentInfo {
    private Long id;
    private String title;
    private String subject;
    private String chapter;
    private LocalDateTime createdAt;

    public static AssignmentInfo from(Assignment assignment) {
        return AssignmentInfo.builder()
                .id(assignment.getId())
                .title(assignment.getDescription())
                .subject(assignment.getSubject())
                .chapter(assignment.getChapter())
                .createdAt(assignment.getCreatedAt())
                .build();
    }
}