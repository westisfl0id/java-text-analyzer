package com.example.textanalyzer.model;

import java.util.Map;
import java.util.Optional;

public record FileAnalysisResult(
        Map<String, Long> wordCounts,
        Optional<FileError> error
) {
    public boolean successful() {
        return error.isEmpty();
    }
}