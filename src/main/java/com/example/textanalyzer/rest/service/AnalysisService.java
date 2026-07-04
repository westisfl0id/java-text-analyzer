package com.example.textanalyzer.rest.service;

import com.example.textanalyzer.cli.AnalysisMode;
import com.example.textanalyzer.cli.CommandLineOptions;
import com.example.textanalyzer.exception.BadArgumentsException;
import com.example.textanalyzer.io.StopWordsLoader;
import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.persistence.entity.AnalysisJobEntity;
import com.example.textanalyzer.persistence.entity.AnalysisStatus;
import com.example.textanalyzer.persistence.repository.AnalysisJobRepository;
import com.example.textanalyzer.rest.dto.AnalysisInfoResponse;
import com.example.textanalyzer.rest.dto.AnalysisResultResponse;
import com.example.textanalyzer.rest.dto.AnalysisSummaryResponse;
import com.example.textanalyzer.rest.dto.AnalyzeRequest;
import com.example.textanalyzer.rest.dto.AnalyzeResponse;
import com.example.textanalyzer.rest.dto.FileErrorResponse;
import com.example.textanalyzer.rest.dto.WordResponse;
import com.example.textanalyzer.service.TextAnalysisService;
import jakarta.annotation.PreDestroy;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Application service that accepts REST analysis requests and maps persisted jobs to DTOs.
 *
 * <p>The service creates a job synchronously, then delegates long-running analysis to a background
 * executor. Database state transitions are handled by {@link AnalysisJobProcessor} to keep them
 * transactional and separated from file processing.</p>
 */
@Service
public class AnalysisService {

    private static final int DEFAULT_THREADS = 2;

    private final AnalysisJobRepository analysisJobRepository;
    private final AnalysisJobProcessor analysisJobProcessor;
    private final TextAnalysisService textAnalysisService;
    private final StopWordsLoader stopWordsLoader;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public AnalysisService(
            @NonNull AnalysisJobRepository analysisJobRepository,
            @NonNull AnalysisJobProcessor analysisJobProcessor,
            @NonNull TextAnalysisService textAnalysisService,
            @NonNull StopWordsLoader stopWordsLoader
    ) {
        this.analysisJobRepository = analysisJobRepository;
        this.analysisJobProcessor = analysisJobProcessor;
        this.textAnalysisService = textAnalysisService;
        this.stopWordsLoader = stopWordsLoader;
    }

    /**
     * Creates a pending analysis job and schedules its execution after transaction commit.
     *
     * @param request analysis parameters received from the REST API
     * @param username authenticated user who requested the analysis
     * @return identifier and initial status of the created job
     */
    @Transactional
    public AnalyzeResponse startAnalysis(@NonNull AnalyzeRequest request, @NonNull String username) {
        AnalysisMode mode = parseMode(request.mode());
        int threads = request.threads() == null ? DEFAULT_THREADS : request.threads();

        if (threads < 1) {
            throw new BadArgumentsException("threads must be greater than 0");
        }

        int effectiveThreads = mode == AnalysisMode.MULTI ? threads : 1;

        AnalysisJobEntity job = new AnalysisJobEntity();
        job.setDirectory(Path.of(request.directory()).normalize().toString());
        job.setMinWordLength(request.minWordLength());
        job.setTopCount(request.topCount());
        job.setMode(mode.cliValue());
        job.setThreads(effectiveThreads);
        job.setStatus(AnalysisStatus.PENDING);
        job.setCreatedBy(username);
        job.setCreatedAt(Instant.now());

        AnalysisJobEntity savedJob = analysisJobRepository.save(job);
        submitAfterCommit(() -> executeAnalysis(savedJob.getId(), request, mode, effectiveThreads));

        return new AnalyzeResponse(savedJob.getId(), savedJob.getStatus().name());
    }

    /**
     * Returns one analysis result with word/error details when the job is already completed.
     *
     * @param id analysis job identifier
     * @return detailed response for the requested job
     */
    @Transactional(readOnly = true)
    public AnalysisResultResponse getResult(@NonNull Long id) {
        AnalysisJobEntity job = findJobWithDetails(id);

        AnalysisInfoResponse analysisInfo = null;
        List<WordResponse> words = Collections.emptyList();
        List<FileErrorResponse> errors = Collections.emptyList();

        if (job.getStatus() == AnalysisStatus.COMPLETED) {
            analysisInfo = new AnalysisInfoResponse(
                    job.getDirectory(),
                    job.getMinWordLength(),
                    job.getTopCount(),
                    job.getMode(),
                    job.getThreads(),
                    job.getProcessedFiles(),
                    job.getExecutionTimeMs()
            );

            words = job.getWords()
                    .stream()
                    .map(word -> new WordResponse(word.getWord(), word.getCountValue()))
                    .toList();

            errors = job.getErrors()
                    .stream()
                    .map(error -> new FileErrorResponse(error.getFile(), error.getMessage()))
                    .toList();
        }

        return new AnalysisResultResponse(
                job.getId(),
                job.getStatus().name(),
                analysisInfo,
                words,
                errors,
                job.getErrorMessage()
        );
    }

    /**
     * Returns summary information for all jobs without loading heavy result collections.
     *
     * @return job summaries ordered from newest to oldest
     */
    @Transactional(readOnly = true)
    public List<AnalysisSummaryResponse> getAllResults() {
        return analysisJobRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(job -> new AnalysisSummaryResponse(
                        job.getId(),
                        job.getStatus().name(),
                        job.getDirectory(),
                        job.getMode(),
                        job.getThreads(),
                        job.getCreatedBy(),
                        job.getCreatedAt(),
                        job.getFinishedAt()
                ))
                .toList();
    }

    private void executeAnalysis(
            @NonNull Long jobId,
            @NonNull AnalyzeRequest request,
            @NonNull AnalysisMode mode,
            int effectiveThreads
    ) {
        if (!analysisJobProcessor.markRunning(jobId)) {
            return;
        }

        try {
            CommandLineOptions options = new CommandLineOptions(
                    Path.of(request.directory()).normalize(),
                    request.minWordLength(),
                    request.topCount(),
                    Optional.empty(),
                    Optional.empty(),
                    mode,
                    effectiveThreads,
                    false
            );

            Set<String> stopWords = prepareStopWords(request);
            AnalysisResult result = textAnalysisService.analyze(options, stopWords);
            analysisJobProcessor.completeJob(jobId, result);
        } catch (Exception exception) {
            String message = exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage();
            analysisJobProcessor.failJob(jobId, message);
        }
    }

    private AnalysisJobEntity findJobWithDetails(@NonNull Long id) {
        return analysisJobRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found: " + id));
    }

    private AnalysisMode parseMode(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return AnalysisMode.SINGLE;
        }

        try {
            return AnalysisMode.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new BadArgumentsException("mode must be either single or multi");
        }
    }

    private Set<String> prepareStopWords(@NonNull AnalyzeRequest request) {
        if (request.stopWordsFile() != null && !request.stopWordsFile().isBlank()) {
            return stopWordsLoader.load(Path.of(request.stopWordsFile()).normalize());
        }

        if (request.stopWords() == null || request.stopWords().isEmpty()) {
            return Set.of();
        }

        return request.stopWords()
                .stream()
                .filter(Objects::nonNull)
                .map(word -> word.trim().toLowerCase(Locale.ROOT))
                .filter(word -> !word.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private void submitAfterCommit(@NonNull Runnable task) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            executorService.submit(task);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                executorService.submit(task);
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}