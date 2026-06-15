package com.example.demo.model;

public record AnalysisInfo(
        String directory,
        int minWordLength,
        int topCount
) {
}
