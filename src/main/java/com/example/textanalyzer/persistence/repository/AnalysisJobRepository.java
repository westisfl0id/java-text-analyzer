package com.example.textanalyzer.persistence.repository;

import com.example.textanalyzer.persistence.entity.AnalysisJobEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for analysis jobs.
 */
public interface AnalysisJobRepository extends JpaRepository<AnalysisJobEntity, Long> {

    /**
     * Returns job summaries ordered from newest to oldest.
     */
    List<AnalysisJobEntity> findAllByOrderByCreatedAtDesc();

    /**
     * Loads one analysis with words and errors to avoid lazy loading issues and N+1 queries.
     */
    @EntityGraph(attributePaths = {"words", "errors"})
    Optional<AnalysisJobEntity> findWithDetailsById(Long id);
}