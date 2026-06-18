package com.example.textanalyzer.execution;

import com.example.textanalyzer.model.FileAnalysisResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface FileAnalysisExecutor {
    List<FileAnalysisResult> analyzeFiles(
            List<Path> files,
            int minWordLength,
            Set<String> stopWords,
            int threads
    );
}
