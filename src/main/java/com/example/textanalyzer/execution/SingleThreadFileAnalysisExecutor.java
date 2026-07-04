package com.example.textanalyzer.execution;

import com.example.textanalyzer.model.FileAnalysisResult;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Sequential file-analysis executor used for single-threaded mode.
 */
@Component
public class SingleThreadFileAnalysisExecutor implements FileAnalysisExecutor {
    private final SingleFileAnalyzer singleFileAnalyzer;

    /**
     * Creates a sequential executor.
     *
     * @param singleFileAnalyzer component that analyzes one file
     */
    public SingleThreadFileAnalysisExecutor(SingleFileAnalyzer singleFileAnalyzer) {
        this.singleFileAnalyzer = singleFileAnalyzer;
    }

    /**
     * Analyzes files one by one in the current thread.
     *
     * @param files files to analyze
     * @param minWordLength minimum accepted word length
     * @param stopWords normalized stop words to exclude
     * @param threads ignored in single-threaded mode
     * @return per-file analysis results
     */
    @Override
    public List<FileAnalysisResult> analyzeFiles(
            List<Path> files,
            int minWordLength,
            Set<String> stopWords,
            int threads
    ) {
        List<FileAnalysisResult> results = new ArrayList<>();

        for (Path file : files) {
            results.add(singleFileAnalyzer.analyze(file, minWordLength, stopWords));
        }

        return results;
    }
}
