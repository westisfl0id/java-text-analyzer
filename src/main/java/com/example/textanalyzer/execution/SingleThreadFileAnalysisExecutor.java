package com.example.textanalyzer.execution;

import com.example.textanalyzer.model.FileAnalysisResult;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SingleThreadFileAnalysisExecutor implements FileAnalysisExecutor {
    private final SingleFileAnalyzer singleFileAnalyzer;

    public SingleThreadFileAnalysisExecutor(SingleFileAnalyzer singleFileAnalyzer) {
        this.singleFileAnalyzer = singleFileAnalyzer;
    }

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
