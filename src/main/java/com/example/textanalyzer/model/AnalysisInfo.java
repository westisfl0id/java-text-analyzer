package com.example.textanalyzer.model;

/**
 * Metadata describing one completed text analysis run.
 *
 * @param directory analyzed directory
 * @param minWordLength minimum accepted word length
 * @param topCount requested number of top words
 * @param mode analysis mode used during execution
 * @param threads effective number of worker threads
 * @param processedFiles number of successfully processed files
 * @param executionTimeMs total execution time in milliseconds
 */
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