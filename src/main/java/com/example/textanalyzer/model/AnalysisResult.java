package com.example.textanalyzer.model;

import java.util.List;

public record AnalysisResult(
        AnalysisInfo analysisInfo,
        List<WordStat> words,
        List<FileError> errors
) {
}
