package com.example.textanalyzer.rest.dto;

public record FileErrorResponse(
        String file,
        String message
) {
}
