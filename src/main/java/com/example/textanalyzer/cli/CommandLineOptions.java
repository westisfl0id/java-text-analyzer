package com.example.textanalyzer.cli;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Immutable command-line configuration used by the analysis service.
 *
 * @param directory directory with source text files
 * @param minWordLength minimum accepted word length
 * @param topCount number of most frequent words to return
 * @param outputFile optional JSON output file
 * @param stopWordsFile optional file with stop words
 * @param mode selected analysis mode
 * @param threads requested number of worker threads
 * @param help whether only help should be printed
 */
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
    /**
     * Creates a special options object that tells the runner to print help only.
     *
     * @return help-mode command-line options
     */
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

    /**
     * Returns the actual number of worker threads that should be used for analysis.
     *
     * @return requested thread count for multi mode, otherwise {@code 1}
     */
    public int effectiveThreads() {
        return mode == AnalysisMode.MULTI ? threads : 1;
    }
}