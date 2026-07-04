package com.example.textanalyzer.model;

/**
 * Represents statistics for a specific word.
 */
public record WordStat(
        String word,
        long count
) {
}
