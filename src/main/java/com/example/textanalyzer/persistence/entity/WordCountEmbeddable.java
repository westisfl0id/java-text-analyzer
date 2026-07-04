package com.example.textanalyzer.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Embeddable object containing word statistics.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class WordCountEmbeddable {
    private String word;

    private long countValue;
}
