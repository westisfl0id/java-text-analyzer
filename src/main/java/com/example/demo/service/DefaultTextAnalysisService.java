package com.example.demo.service;

import com.example.demo.cli.CommandLineOptions;
import com.example.demo.exception.InvalidArgumentsException;
import com.example.demo.io.TextFileReader;
import com.example.demo.model.AnalysisInfo;
import com.example.demo.model.AnalysisResult;
import com.example.demo.model.FileError;
import com.example.demo.model.WordStat;
import com.example.demo.word.WordExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DefaultTextAnalysisService implements TextAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTextAnalysisService.class);

    private final TextFileReader textFileReader;
    private final WordExtractor wordExtractor;

    public DefaultTextAnalysisService(
            TextFileReader textFileReader,
            WordExtractor wordExtractor
    ) {
        this.textFileReader = textFileReader;
        this.wordExtractor = wordExtractor;
    }

    @Override
    public AnalysisResult analyze(CommandLineOptions options, Set<String> stopWords) {
        validateDirectory(options.directory());

        Map<String, Long> wordCounts = new HashMap<>();
        List<FileError> errors = new ArrayList<>();
        List<Path> textFiles = findTextFiles(options.directory());

        if (textFiles.isEmpty()) {
            log.info("В папке не найдено .txt файлов: {}", options.directory());
        }

        for (Path textFile : textFiles) {
            analyzeFile(
                    textFile,
                    options.minWordLength(),
                    stopWords,
                    wordCounts,
                    errors
            );
        }

        List<WordStat> words = wordCounts.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                                .thenComparing(Map.Entry.comparingByKey())
                )
                .limit(options.topCount())
                .map(entry -> new WordStat(entry.getKey(), entry.getValue()))
                .toList();

        AnalysisInfo analysisInfo = new AnalysisInfo(
                options.directory().toString(),
                options.minWordLength(),
                options.topCount()
        );

        return new AnalysisResult(
                analysisInfo,
                words,
                List.copyOf(errors)
        );
    }

    private void validateDirectory(Path directory) {
        if (directory == null) {
            throw new InvalidArgumentsException("Путь к папке не указан");
        }

        if (!Files.exists(directory)) {
            throw new InvalidArgumentsException("Папка не найдена: " + directory);
        }

        if (!Files.isDirectory(directory)) {
            throw new InvalidArgumentsException("Указанный путь не является папкой: " + directory);
        }
    }

    private List<Path> findTextFiles(Path directory) {
        try {
            return Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .filter(this::isTxtFile)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать папку: " + directory, e);
        }
    }

    private boolean isTxtFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".txt");
    }

    private void analyzeFile(
            Path file,
            int minWordLength,
            Set<String> stopWords,
            Map<String, Long> wordCounts,
            List<FileError> errors
    ) {
        try {
            String text = textFileReader.read(file);

            if (text.isBlank()) {
                errors.add(new FileError(file.toString(), "Файл пустой"));
                return;
            }

            List<String> words = wordExtractor.extractWords(text);

            for (String word : words) {
                if (word.length() < minWordLength) {
                    continue;
                }

                if (stopWords.contains(word)) {
                    continue;
                }

                wordCounts.merge(word, 1L, Long::sum);
            }

        } catch (IOException e) {
            errors.add(new FileError(file.toString(), "Не удалось прочитать файл: " + e.getMessage()));
            log.warn("Cannot read file: {}", file, e);
        }
    }
}