package com.example.textanalyzer.model;

/**
 * Represents an error that occurred while processing a file.
 */
public record FileError(
        String file,
        String message
) {
}
