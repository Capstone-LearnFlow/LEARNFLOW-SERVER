package com.learnflow.learnflowserver.repository;

import com.learnflow.learnflowserver.domain.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    List<Evidence> findByNodeId(Long nodeId);
}
