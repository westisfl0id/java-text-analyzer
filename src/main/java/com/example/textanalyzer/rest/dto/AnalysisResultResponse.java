package com.example.textanalyzer.rest.dto;

import java.util.List;

/**
 * Response containing complete analysis results.
 */
public record AnalysisResultResponse(
        Long id,
        String status,
        AnalysisInfoResponse analysisInfo,
        List<WordResponse> words,
        List<FileErrorResponse> errors,
        String errorMessage
) {
}
