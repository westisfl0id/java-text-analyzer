package com.example.textanalyzer.service;

import com.example.textanalyzer.cli.AnalysisMode;
import com.example.textanalyzer.cli.CommandLineOptions;
import com.example.textanalyzer.exception.BadArgumentsException;
import com.example.textanalyzer.execution.FileAnalysisExecutor;
import com.example.textanalyzer.execution.PooledFileAnalysisExecutor;
import com.example.textanalyzer.execution.SingleThreadFileAnalysisExecutor;
import com.example.textanalyzer.io.TextFileReader;
import com.example.textanalyzer.model.AnalysisInfo;
import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.model.FileAnalysisResult;
import com.example.textanalyzer.model.FileError;
import com.example.textanalyzer.model.WordStat;
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
import java.util.concurrent.TimeUnit;

@Service
public class DefaultTextAnalysisService implements TextAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTextAnalysisService.class);

    private final TextFileReader textFileReader;
    private final SingleThreadFileAnalysisExecutor singleThreadExecutor;
    private final PooledFileAnalysisExecutor pooledExecutor;

    public DefaultTextAnalysisService(
            TextFileReader textFileReader,
            SingleThreadFileAnalysisExecutor singleThreadExecutor,
            PooledFileAnalysisExecutor pooledExecutor
    ) {
        this.textFileReader = textFileReader;
        this.singleThreadExecutor = singleThreadExecutor;
        this.pooledExecutor = pooledExecutor;
    }

    @Override
    public AnalysisResult analyze(CommandLineOptions options, Set<String> stopWords) {
        validateDirectory(options.directory());

        long startTime = System.nanoTime();

        List<Path> textFiles = findTextFiles(options.directory());

        if (textFiles.isEmpty()) {
            log.warn("No .txt files found in directory {}", options.directory());
        }

        FileAnalysisExecutor executor = selectExecutor(options.mode());

        List<FileAnalysisResult> fileResults = executor.analyzeFiles(
                textFiles,
                options.minWordLength(),
                stopWords,
                options.effectiveThreads()
        );

        Map<String, Long> wordCounts = new HashMap<>();
        List<FileError> errors = new ArrayList<>();
        int processedFiles = 0;

        for (FileAnalysisResult fileResult : fileResults) {
            if (fileResult.successful()) {
                processedFiles++;
            }

            fileResult.error().ifPresent(errors::add);
            mergeWordCounts(wordCounts, fileResult.wordCounts());
        }

        long executionTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        List<WordStat> topWords = wordCounts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(options.topCount())
                .map(entry -> new WordStat(entry.getKey(), entry.getValue()))
                .toList();

        AnalysisInfo analysisInfo = new AnalysisInfo(
                options.directory().toString(),
                options.minWordLength(),
                options.topCount(),
                options.mode().cliValue(),
                options.effectiveThreads(),
                processedFiles,
                executionTimeMs
        );

        return new AnalysisResult(analysisInfo, topWords, List.copyOf(errors));
    }

    private FileAnalysisExecutor selectExecutor(AnalysisMode mode) {
        return switch (mode) {
            case SINGLE -> singleThreadExecutor;
            case MULTI -> pooledExecutor;
        };
    }

    private void mergeWordCounts(Map<String, Long> target, Map<String, Long> source) {
        source.forEach((word, count) -> target.merge(word, count, Long::sum));
    }

    private List<Path> findTextFiles(Path directory) {
        try {
            return textFileReader.findTextFiles(directory);
        } catch (IOException exception) {
            throw new BadArgumentsException("Cannot read directory: " + directory);
        }
    }

    private void validateDirectory(Path directory) {
        if (!Files.exists(directory)) {
            throw new BadArgumentsException("Directory does not exist: " + directory);
        }

        if (!Files.isDirectory(directory)) {
            throw new BadArgumentsException("Path is not a directory: " + directory);
        }

        if (!Files.isReadable(directory)) {
            throw new BadArgumentsException("Directory is not readable: " + directory);
        }
    }
}