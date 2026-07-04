package com.example.textanalyzer.word;

import java.util.List;

/**
 * Contract for converting raw text into normalized words.
 */
public interface WordExtractor {

    /**
     * Extracts normalized words from raw text.
     *
     * @param text source text
     * @return words in source order
     */
    List<String> extractWords(String text);
}
