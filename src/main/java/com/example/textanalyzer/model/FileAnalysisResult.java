package com.example.textanalyzer.model;

import java.util.Map;
import java.util.Optional;

/**
 * Result of processing one input file.
 *
 * @param wordCounts calculated word frequencies
 * @param error optional processing error
 */
public record FileAnalysisResult(
        Map<String, Long> wordCounts,
        Optional<FileError> error
) {
    public boolean successful() {
        return error.isEmpty();
    }
}