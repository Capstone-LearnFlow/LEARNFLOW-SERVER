package com.learnflow.learnflowserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class StudentTreeLogResponse {
    private AssignmentInfo assignment;
    private StudentInfo student;
    private List<ActivityLogResponse> activities; // TreeLogResponse -> ActivityLogResponse
    private ActivityStatistics statistics; // LogStatistics -> ActivityStatistics
}