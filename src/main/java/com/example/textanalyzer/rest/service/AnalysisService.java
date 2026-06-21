package com.example.textanalyzer.rest.service;

import com.example.textanalyzer.cli.AnalysisMode;
import com.example.textanalyzer.cli.CommandLineOptions;
import com.example.textanalyzer.exception.BadArgumentsException;
import com.example.textanalyzer.io.StopWordsLoader;
import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.persistence.entity.AnalysisJobEntity;
import com.example.textanalyzer.persistence.entity.AnalysisStatus;
import com.example.textanalyzer.persistence.entity.AuditLogEntity;
import com.example.textanalyzer.persistence.entity.FileErrorEmbeddable;
import com.example.textanalyzer.persistence.entity.WordCountEmbeddable;
import com.example.textanalyzer.persistence.repository.AnalysisJobRepository;
import com.example.textanalyzer.persistence.repository.AuditLogRepository;
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
import org.springframework.stereotype.Service;
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

@Service
public class AnalysisService {

    private static final int DEFAULT_THREADS = 2;

    private final AnalysisJobRepository analysisJobRepository;
    private final AuditLogRepository auditLogRepository;
    private final TextAnalysisService textAnalysisService;
    private final StopWordsLoader stopWordsLoader;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public AnalysisService(
            AnalysisJobRepository analysisJobRepository,
            AuditLogRepository auditLogRepository,
            TextAnalysisService textAnalysisService,
            StopWordsLoader stopWordsLoader
    ) {
        this.analysisJobRepository = analysisJobRepository;
        this.auditLogRepository = auditLogRepository;
        this.textAnalysisService = textAnalysisService;
        this.stopWordsLoader = stopWordsLoader;
    }

    public AnalyzeResponse startAnalysis(AnalyzeRequest request, String username) {
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

        auditLogRepository.save(new AuditLogEntity(
                username,
                "START_ANALYSIS",
                Instant.now(),
                buildAuditParameters(request, mode, effectiveThreads)
        ));

        executorService.submit(() -> executeAnalysis(savedJob.getId(), request, mode, threads));

        return new AnalyzeResponse(savedJob.getId(), savedJob.getStatus().name());
    }

    public AnalysisResultResponse getResult(Long id) {
        AnalysisJobEntity job = findJob(id);

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
            Long jobId,
            AnalyzeRequest request,
            AnalysisMode mode,
            int threads
    ) {
        AnalysisJobEntity job = analysisJobRepository.findById(jobId).orElse(null);

        if (job == null) {
            return;
        }

        try {
            job.setStatus(AnalysisStatus.RUNNING);
            job.setStartedAt(Instant.now());
            analysisJobRepository.save(job);

            CommandLineOptions options = new CommandLineOptions(
                    Path.of(request.directory()).normalize(),
                    request.minWordLength(),
                    request.topCount(),
                    Optional.empty(),
                    Optional.empty(),
                    mode,
                    threads,
                    false
            );

            Set<String> stopWords = prepareStopWords(request);

            AnalysisResult result = textAnalysisService.analyze(options, stopWords);

            job.setStatus(AnalysisStatus.COMPLETED);
            job.setDirectory(result.analysisInfo().directory());
            job.setMinWordLength(result.analysisInfo().minWordLength());
            job.setTopCount(result.analysisInfo().topCount());
            job.setMode(result.analysisInfo().mode());
            job.setThreads(result.analysisInfo().threads());
            job.setProcessedFiles(result.analysisInfo().processedFiles());
            job.setExecutionTimeMs(result.analysisInfo().executionTimeMs());
            job.setFinishedAt(Instant.now());

            job.getWords().clear();
            result.words().forEach(word ->
                    job.getWords().add(new WordCountEmbeddable(word.word(), word.count()))
            );

            job.getErrors().clear();
            result.errors().forEach(error ->
                    job.getErrors().add(new FileErrorEmbeddable(error.file(), error.message()))
            );

            analysisJobRepository.save(job);
        } catch (Exception exception) {
            job.setStatus(AnalysisStatus.FAILED);
            job.setErrorMessage(exception.getMessage());
            job.setFinishedAt(Instant.now());
            analysisJobRepository.save(job);
        }
    }

    private AnalysisJobEntity findJob(Long id) {
        return analysisJobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found: " + id));
    }

    private AnalysisMode parseMode(String value) {
        if (value == null || value.isBlank()) {
            return AnalysisMode.SINGLE;
        }

        try {
            return AnalysisMode.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new BadArgumentsException("mode must be either single or multi");
        }
    }

    private Set<String> prepareStopWords(AnalyzeRequest request) {
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

    private String buildAuditParameters(
            AnalyzeRequest request,
            AnalysisMode mode,
            int threads
    ) {
        return "directory=" + request.directory()
                + ", minWordLength=" + request.minWordLength()
                + ", topCount=" + request.topCount()
                + ", mode=" + mode.cliValue()
                + ", threads=" + threads
                + ", stopWordsFile=" + request.stopWordsFile();
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}