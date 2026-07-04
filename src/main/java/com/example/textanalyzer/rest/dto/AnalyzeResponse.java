package com.example.textanalyzer.rest.dto;

/**
 * Response returned after submitting files for analysis.
 */
public record AnalyzeResponse(
        Long id,
        String status
) {
}
