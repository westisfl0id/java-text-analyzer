package com.example.textanalyzer.rest.dto;

public record AnalysisInfoResponse(
        String directory,
        int minWordLength,
        int topCount,
        String mode,
        int threads,
        int processedFiles,
        long executionTimeMs
) {
}
