package com.example.textanalyzer.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;
import java.util.List;

/**
 * Request body for starting text analysis.
 *
 * @param directory directory with text files
 * @param minWordLength minimum word length to include
 * @param topCount maximum number of popular words to return
 * @param mode analysis mode: single or multi
 * @param threads number of worker threads for multi-threaded mode
 * @param stopWords stop words passed directly in request
 * @param stopWordsFile optional path to stop words file
 */
public record AnalyzeRequest(
        @NotBlank(message = "directory must not be blank")
        String directory,
        @Min(value = 1, message = "minWordLength must be greater than or equal to 1")
        int minWordLength,
        @Min(value = 1, message = "topCount must be greater than or equal to 1")
        int topCount,
        @Nullable String mode,
        @Nullable Integer threads,
        @Nullable String stopWordsFile,
        @Nullable List<String> stopWords
) {
}