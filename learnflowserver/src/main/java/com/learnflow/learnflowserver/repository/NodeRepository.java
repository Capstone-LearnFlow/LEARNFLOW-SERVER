package com.learnflow.learnflowserver.repository;

import com.learnflow.learnflowserver.domain.Node;
import com.learnflow.learnflowserver.domain.StudentAssignment;
import com.learnflow.learnflowserver.domain.common.enums.NodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Long> {
    List<Node> findByStudentAssignmentAndIsHiddenFalse(StudentAssignment studentAssignment);
    List<Node> findByStudentAssignment(StudentAssignment studentAssignment);
    List<Node> findByStudentAssignmentOrderByCreatedAtAsc(StudentAssignment studentAssignment);

    @Query("SELECT n FROM Node n WHERE n.studentAssignment.assignment.id = :assignmentId " +
            "ORDER BY n.createdAt DESC")
    List<Node> findByAssignmentIdOrderByCreatedAtDesc(@Param("assignmentId") Long assignmentId);

    List<Node> findByParentAndIsHiddenFalse(Node parent);

}
