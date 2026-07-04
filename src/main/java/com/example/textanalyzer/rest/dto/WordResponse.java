package com.example.textanalyzer.rest.dto;

/**
 * Represents information about a word and its occurrence count.
 */
public record WordResponse(
        String word,
        long count
) {
}
