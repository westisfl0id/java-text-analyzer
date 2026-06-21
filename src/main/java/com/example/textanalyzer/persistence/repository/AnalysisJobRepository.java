package com.example.textanalyzer.persistence.repository;

import com.example.textanalyzer.persistence.entity.AnalysisJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJobEntity, Long> {
    List<AnalysisJobEntity> findAllByOrderByCreatedAtDesc();
}
