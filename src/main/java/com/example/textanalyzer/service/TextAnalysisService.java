package com.example.textanalyzer.service;

import com.example.textanalyzer.cli.CommandLineOptions;
import com.example.textanalyzer.model.AnalysisResult;
import java.util.Set;

/**
 * Main text-analysis use-case contract.
 */
public interface TextAnalysisService {

    /**
     * Executes text analysis for the provided options and stop words.
     *
     * @param options analysis configuration
     * @param stopWords normalized stop words to exclude
     * @return aggregated analysis result
     */
    AnalysisResult analyze(CommandLineOptions options, Set<String> stopWords);
}