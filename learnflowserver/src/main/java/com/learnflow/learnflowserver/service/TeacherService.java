package com.learnflow.learnflowserver.service;

import com.learnflow.learnflowserver.domain.common.enums.Role;
import com.learnflow.learnflowserver.domain.common.enums.Status;
import com.learnflow.learnflowserver.domain.common.enums.StudentStatus;
import com.learnflow.learnflowserver.dto.AssignmentSummaryForTeacherDto;
import com.learnflow.learnflowserver.dto.StudentDto;
import com.learnflow.learnflowserver.dto.request.AssignmentCreateRequest;
import com.learnflow.learnflowserver.dto.PhaseRequestDto;
import com.learnflow.learnflowserver.domain.*;
import com.learnflow.learnflowserver.repository.AssignmentRepository;
import com.learnflow.learnflowserver.repository.PhaseRepository;
import com.learnflow.learnflowserver.repository.StudentAssignmentRepository;
import com.learnflow.learnflowserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {

    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentAssignmentRepository studentAssignmentRepository;
    private final PhaseRepository phaseRepository;

    public List<StudentDto> getAllStudents() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.STUDENT)
                .map(StudentDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createAssignment(AssignmentCreateRequest request, Long teacherId) {
        // 교사 조회
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        if (teacher.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("교사 권한이 필요합니다.");
        }

        // 과제 생성
        Assignment assignment = new Assignment(
                request.getSubject(),
                request.getChapter(),
                request.getTopic(),
                request.getDescription(),
                Status.ACTIVE,
                teacher
        );

        assignmentRepository.save(assignment);

        // 단계 생성
        for (PhaseRequestDto phaseDto : request.getPhases()) {
            Phase phase = new Phase(
                    assignment,
                    phaseDto.getPhaseNumber(),
                    phaseDto.getStartDate(),
                    phaseDto.getEndDate(),
                    Status.INACTIVE // 초기 상태는 INACTIVE
            );
            phaseRepository.save(phase);
        }

        // 학생에게 과제 할당
        for (Long studentId : request.getStudentIds()) {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학생입니다."));

            if (student.getRole() != Role.STUDENT) {
                throw new IllegalArgumentException("학생에게만 과제를 할당할 수 있습니다.");
            }

            // 학생-과제 정보 생성 (초기 단계는 첫번째 단계, 상태는 NOT_STARTED)
            Integer initialPhase = request.getPhases().stream()
                    .map(PhaseRequestDto::getPhaseNumber)
                    .min(Integer::compareTo)
                    .orElse(1);

            StudentAssignment studentAssignment = new StudentAssignment(
                    student,
                    assignment,
                    initialPhase,
                    StudentStatus.NOT_STARTED
            );

            studentAssignmentRepository.save(studentAssignment);
        }

        return assignment.getId();
    }

    public List<AssignmentSummaryForTeacherDto> getAssignmentsByTeacherId(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Assignment> assignments = assignmentRepository.findByCreatedByOrderByCreatedAtDesc(teacher);

        return assignments.stream()
                .map(assignment -> {
                    // 학생 수 계산
                    List<StudentAssignment> studentAssignments = studentAssignmentRepository.findByAssignmentId(assignment.getId());
                    int studentCount = studentAssignments.size();

                    // 현재 단계 계산 (학생들의 현재 단계 중 가장 많은 단계)
                    Integer currentPhase = studentAssignments.stream()
                            .collect(Collectors.groupingBy(StudentAssignment::getCurrentPhase, Collectors.counting()))
                            .entrySet().stream()
                            .max(java.util.Map.Entry.comparingByValue())
                            .map(java.util.Map.Entry::getKey)
                            .orElse(1);

                    return AssignmentSummaryForTeacherDto.builder()
                            .id(assignment.getId())
                            .subject(assignment.getSubject())
                            .chapter(assignment.getChapter())
                            .topic(assignment.getTopic())
                            .createdAt(assignment.getCreatedAt())
                            .studentCount(studentCount)
                            .currentPhase(currentPhase)
                            .build();
                })
                .collect(Collectors.toList());
    }
}