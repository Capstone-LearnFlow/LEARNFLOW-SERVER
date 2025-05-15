package com.learnflow.learnflowserver.repository;

import com.learnflow.learnflowserver.domain.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}