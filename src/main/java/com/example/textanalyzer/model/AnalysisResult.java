package com.example.textanalyzer.model;

import java.util.List;

/**
 * Final aggregated result of a text analysis run.
 *
 * @param analysisInfo metadata of the analysis
 * @param words most frequent words
 * @param errors file-processing errors collected during analysis
 */
public record AnalysisResult(
        AnalysisInfo analysisInfo,
        List<WordStat> words,
        List<FileError> errors
) {
}
