package com.example.textanalyzer.audit;

import com.example.textanalyzer.persistence.entity.AuditLogEntity;
import com.example.textanalyzer.persistence.repository.AuditLogRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

/**
 * Stores technical audit events separately from business logic.
 */
@Service
public class AuditService {

    private static final int MAX_PARAMETERS_LENGTH = 2000;

    private final AuditLogRepository auditLogRepository;

    public AuditService(@NonNull AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Saves an audit event in a separate transaction.
     *
     * @param username user who initiated the operation
     * @param action audit action code
     * @param parameters optional serialized request parameters
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveEvent(
            @NonNull String username,
            @NonNull String action,
            @Nullable String parameters
    ) {
        auditLogRepository.save(new AuditLogEntity(
                username,
                action,
                Instant.now(),
                truncate(parameters)
        ));
    }

    @Nullable
    private String truncate(@Nullable String value) {
        if (value == null || value.length() <= MAX_PARAMETERS_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_PARAMETERS_LENGTH);
    }
}