package com.example.textanalyzer.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AnalyzeRequest(
        @NotBlank String directory,
        @Min(1) int minWordLength,
        @Min(1) int topCount,
        String mode,
        Integer threads,
        String stopWordsFile,
        List<String> stopWords
) {
}