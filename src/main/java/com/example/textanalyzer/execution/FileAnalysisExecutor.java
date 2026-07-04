package com.example.textanalyzer.execution;

import com.example.textanalyzer.model.FileAnalysisResult;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Strategy interface for analyzing a collection of text files.
 */
public interface FileAnalysisExecutor {

    /**
     * Analyzes all provided files and returns one result per file.
     *
     * @param files files to analyze
     * @param minWordLength minimum accepted word length
     * @param stopWords normalized stop words to exclude
     * @param threads requested number of worker threads
     * @return per-file analysis results
     */
    List<FileAnalysisResult> analyzeFiles(
            List<Path> files,
            int minWordLength,
            Set<String> stopWords,
            int threads
    );
}
