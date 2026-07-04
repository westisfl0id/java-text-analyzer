package com.example.textanalyzer.word;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts words from text using a Unicode-aware regular expression.
 */
@Component
public class RegexWordExtractor implements WordExtractor {

    private static final Pattern WORD_PATTERN = Pattern.compile("\\p{L}+");

    /**
     * Extracts letter-only words and lowercases them with the root locale.
     *
     * @param text source text
     * @return extracted normalized words in source order
     */
    @Override
    public List<String> extractWords(String text) {
        List<String> words = new ArrayList<>();
        Matcher matcher = WORD_PATTERN.matcher(text.toLowerCase(Locale.ROOT));

        while (matcher.find()) {
            words.add(matcher.group());
        }

        return words;
    }
}