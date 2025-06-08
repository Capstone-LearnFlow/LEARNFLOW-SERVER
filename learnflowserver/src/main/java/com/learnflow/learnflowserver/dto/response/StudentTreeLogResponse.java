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
    private NodeTreeLogResponse treeStructure; // activities 대신 treeStructure
    private ActivityStatistics statistics;
}