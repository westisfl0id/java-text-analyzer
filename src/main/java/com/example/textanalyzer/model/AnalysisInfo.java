package com.example.textanalyzer.model;

public record AnalysisInfo(
        String directory,
        int minWordLength,
        int topCount,
        String mode,
        int threads,
        int processedFiles,
        long executionTimeMs
) {
}