package com.example.textanalyzer.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Embeddable object describing a file processing error.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FileErrorEmbeddable {
    private String file;

    private String message;
}
