package com.example.textanalyzer.execution;

import com.example.textanalyzer.io.TextFileReader;
import com.example.textanalyzer.model.FileAnalysisResult;
import com.example.textanalyzer.model.FileError;
import com.example.textanalyzer.word.WordExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class SingleFileAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(SingleFileAnalyzer.class);

    private final TextFileReader textFileReader;
    private final WordExtractor wordExtractor;

    public SingleFileAnalyzer(TextFileReader textFileReader, WordExtractor wordExtractor) {
        this.textFileReader = textFileReader;
        this.wordExtractor = wordExtractor;
    }

    public FileAnalysisResult analyze(Path file, int minWordLength, Set<String> stopWords) {
        Map<String, Long> wordCounts = new HashMap<>();

        try {
            String text = textFileReader.readFile(file);

            wordExtractor.extractWords(text)
                    .stream()
                    .filter(word -> word.length() >= minWordLength)
                    .filter(word -> !stopWords.contains(word))
                    .forEach(word -> wordCounts.merge(word, 1L, Long::sum));

            return new FileAnalysisResult(Map.copyOf(wordCounts), Optional.empty());
        } catch (IOException exception) {
            log.warn("Cannot read file {}: {}", file, exception.getMessage());

            return new FileAnalysisResult(
                    Map.of(),
                    Optional.of(new FileError(file.toString(), exception.getMessage()))
            );
        }
    }
}
