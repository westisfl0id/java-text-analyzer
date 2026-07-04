package com.example.textanalyzer.rest.dto;

/**
 * Represents information about an error that occurred during file processing.
 */
public record FileErrorResponse(
        String file,
        String message
) {
}
