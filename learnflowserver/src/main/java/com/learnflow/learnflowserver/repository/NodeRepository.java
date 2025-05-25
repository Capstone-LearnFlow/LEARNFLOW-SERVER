package com.learnflow.learnflowserver.repository;

import com.learnflow.learnflowserver.domain.Node;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<Node, Long> {
}
