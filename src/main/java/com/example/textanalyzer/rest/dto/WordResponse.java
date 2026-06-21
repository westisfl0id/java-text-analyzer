package com.example.textanalyzer.rest.dto;

public record WordResponse(
        String word,
        long count
) {
}
