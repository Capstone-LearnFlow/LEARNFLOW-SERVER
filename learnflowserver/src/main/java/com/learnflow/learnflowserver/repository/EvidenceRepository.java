package com.learnflow.learnflowserver.repository;

import com.learnflow.learnflowserver.domain.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
}
