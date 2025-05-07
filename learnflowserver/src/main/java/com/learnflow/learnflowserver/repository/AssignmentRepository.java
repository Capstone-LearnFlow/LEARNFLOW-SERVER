package com.learnflow.learnflowserver.repository;

import com.learnflow.learnflowserver.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
}
