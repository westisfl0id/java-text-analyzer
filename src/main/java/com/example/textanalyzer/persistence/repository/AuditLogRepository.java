package com.example.textanalyzer.persistence.repository;

import com.example.textanalyzer.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for storing and reading audit log entries.
 */
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
}