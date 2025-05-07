package com.learnflow.learnflowserver.service;

import com.learnflow.learnflowserver.dto.AssignmentDetailDto;
import com.learnflow.learnflowserver.dto.AssignmentSummaryDto;
import com.learnflow.learnflowserver.dto.PhaseInfoDto;
import com.learnflow.learnflowserver.entity.*;
import com.learnflow.learnflowserver.repository.AssignmentRepository;
import com.learnflow.learnflowserver.repository.PhaseRepository;
import com.learnflow.learnflowserver.repository.StudentAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentAssignmentRepository studentAssignmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final PhaseRepository phaseRepository;

    public List<AssignmentSummaryDto> getAssignmentsByStudentId(Long studentId) {
        List<StudentAssignment> studentAssignments = studentAssignmentRepository.findByStudentId(studentId);

        return studentAssignments.stream()
                .map(sa -> {
                    Assignment assignment = sa.getAssignment();
                    User teacher = assignment.getCreatedBy();

                    // 현재 단계의 종료일 찾기
                    LocalDateTime phaseEndDate = phaseRepository.findByAssignmentId(assignment.getId()).stream()
                            .filter(phase -> phase.getPhaseNumber().equals(sa.getCurrentPhase()))
                            .findFirst()
                            .map(Phase::getEndDate)
                            .orElse(null);

                    return AssignmentSummaryDto.builder()
                            .id(assignment.getId())
                            .subject(assignment.getSubject())
                            .chapter(assignment.getChapter())
                            .topic(assignment.getTopic())
                            .assignmentStatus(assignment.getStatus())
                            .teacherName(teacher.getName())
                            .currentPhase(sa.getCurrentPhase())
                            .studentStatus(sa.getStatus())
                            .phaseEndDate(phaseEndDate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public AssignmentDetailDto getAssignmentDetail(Long assignmentId, Long studentId) {
        // 과제 정보 조회
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과제입니다."));

        // 학생-과제 정보 조회
        StudentAssignment studentAssignment = studentAssignmentRepository.findByStudentIdAndAssignmentId(studentId, assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("학생에게 할당되지 않은 과제입니다."));

        // 단계 정보 조회
        List<Phase> phases = phaseRepository.findByAssignmentIdOrderByPhaseNumber(assignmentId);

        List<PhaseInfoDto> phaseInfos = phases.stream()
                .map(phase -> PhaseInfoDto.builder()
                        .phaseNumber(phase.getPhaseNumber())
                        .status(getPhaseStatus(phase, studentAssignment.getCurrentPhase()))
                        .startDate(phase.getStartDate())
                        .endDate(phase.getEndDate())
                        .build())
                .collect(Collectors.toList());

        return AssignmentDetailDto.builder()
                .id(assignment.getId())
                .subject(assignment.getSubject())
                .chapter(assignment.getChapter())
                .topic(assignment.getTopic())
                .description(assignment.getDescription())
                .teacherName(assignment.getCreatedBy().getName())
                .phases(phaseInfos)
                .build();
    }

    // 단계 상태 결정 (학생의 현재 단계와 비교하여 COMPLETED/IN_PROGRESS/NOT_STARTED 결정)
    private Status getPhaseStatus(Phase phase, Integer currentPhase) {
        if (phase.getPhaseNumber() < currentPhase) {
            return Status.COMPLETED;
        } else if (phase.getPhaseNumber().equals(currentPhase)) {
            return Status.ACTIVE; // 현재 단계는 ACTIVE (IN_PROGRESS)
        } else {
            return Status.INACTIVE; // 아직 시작하지 않은 단계는 INACTIVE (NOT_STARTED)
        }
    }
}