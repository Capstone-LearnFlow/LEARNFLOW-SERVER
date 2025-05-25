package com.learnflow.learnflowserver.repository;

import com.learnflow.learnflowserver.domain.Phase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhaseRepository extends JpaRepository<Phase, Long> {
    List<Phase> findByAssignmentId(Long assignmentId);
    List<Phase> findByAssignmentIdOrderByPhaseNumber(Long assignmentId);
}