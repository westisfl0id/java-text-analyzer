package com.example.textanalyzer.rest.dto;

import java.time.Instant;

/**
 * Response containing summary statistics of an analysis.
 */
public record AnalysisSummaryResponse(
        Long id,
        String status,
        String directory,
        String mode,
        int threads,
        String createdBy,
        Instant createdAt,
        Instant finishedAt
) {
}