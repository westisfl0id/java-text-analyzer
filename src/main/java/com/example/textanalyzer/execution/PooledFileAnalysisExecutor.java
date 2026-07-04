package com.example.textanalyzer.execution;

import com.example.textanalyzer.exception.BadArgumentsException;
import com.example.textanalyzer.model.FileAnalysisResult;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Multi-threaded implementation that analyzes files in a bounded worker pool.
 */
@Component
public class PooledFileAnalysisExecutor implements FileAnalysisExecutor {
    private final SingleFileAnalyzer singleFileAnalyzer;

    /**
     * Creates a pooled executor that delegates single-file processing to the given analyzer.
     *
     * @param singleFileAnalyzer component that analyzes one file
     */
    public PooledFileAnalysisExecutor(SingleFileAnalyzer singleFileAnalyzer) {
        this.singleFileAnalyzer = singleFileAnalyzer;
    }

    /**
     * Analyzes files concurrently using the requested number of worker threads.
     *
     * @param files files to analyze
     * @param minWordLength minimum accepted word length
     * @param stopWords normalized stop words to exclude
     * @param threads size of the worker pool
     * @return per-file analysis results
     */
    @Override
    public List<FileAnalysisResult> analyzeFiles(
            List<Path> files,
            int minWordLength,
            Set<String> stopWords,
            int threads
    ) {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        try {
            List<Callable<FileAnalysisResult>> tasks = files.stream()
                    .map(file -> (Callable<FileAnalysisResult>) () ->
                            singleFileAnalyzer.analyze(file, minWordLength, stopWords)
                    )
                    .toList();

            List<Future<FileAnalysisResult>> futures = executorService.invokeAll(tasks);
            List<FileAnalysisResult> results = new ArrayList<>(futures.size());

            for (Future<FileAnalysisResult> future : futures) {
                results.add(readResult(future));
            }

            return results;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BadArgumentsException("Parallel analysis was interrupted");
        } finally {
            executorService.shutdown();
        }
    }

    private FileAnalysisResult readResult(Future<FileAnalysisResult> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BadArgumentsException("Parallel analysis was interrupted");
        } catch (ExecutionException exception) {
            throw new BadArgumentsException("Parallel analysis failed: " + exception.getCause().getMessage());
        }
    }
}
