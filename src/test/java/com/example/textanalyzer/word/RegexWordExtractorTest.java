package com.example.textanalyzer.word;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegexWordExtractorTest {

    private final RegexWordExtractor extractor = new RegexWordExtractor();

    @Test
    void extractsOnlyLettersAndLowercasesWords() {
        assertThat(extractor.extractWords("Hello, WORLD! Java-17 и Текст."))
                .containsExactly("hello", "world", "java", "и", "текст");
    }

    @Test
    void returnsEmptyListForTextWithoutWords() {
        assertThat(extractor.extractWords("123 !? ---"))
                .isEmpty();
    }
}