package com.example.textanalyzer.cli;

import java.nio.file.Path;
import java.util.Optional;

public record CommandLineOptions(
        Path directory,
        int minWordLength,
        int topCount,
        Optional<Path> outputFile,
        Optional<Path> stopWordsFile,
        AnalysisMode mode,
        int threads,
        boolean help
) {
    public static CommandLineOptions helpOptions() {
        return new CommandLineOptions(
                null,
                0,
                0,
                Optional.empty(),
                Optional.empty(),
                AnalysisMode.SINGLE,
                1,
                true
        );
    }

    public int effectiveThreads() {
        return mode == AnalysisMode.MULTI ? threads : 1;
    }
}