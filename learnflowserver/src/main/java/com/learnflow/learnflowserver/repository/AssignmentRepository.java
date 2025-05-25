package com.learnflow.learnflowserver.repository;

import com.learnflow.learnflowserver.domain.Assignment;
import com.learnflow.learnflowserver.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    List<Assignment> findByCreatedByOrderByCreatedAtDesc(User createdBy);
}
