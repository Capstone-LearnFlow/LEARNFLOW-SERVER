package com.learnflow.learnflowserver.repository;

import com.learnflow.learnflowserver.entity.StudentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentAssignmentRepository extends JpaRepository<StudentAssignment,Long> {
    List<StudentAssignment> findByStudentId(Long studentId);

    Optional<StudentAssignment> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);
}