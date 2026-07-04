package com.example.textanalyzer.rest.service;

import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.persistence.entity.AnalysisJobEntity;
import com.example.textanalyzer.persistence.entity.AnalysisStatus;
import com.example.textanalyzer.persistence.entity.FileErrorEmbeddable;
import com.example.textanalyzer.persistence.entity.WordCountEmbeddable;
import com.example.textanalyzer.persistence.repository.AnalysisJobRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Performs transactional state changes for analysis jobs.
 *
 * <p>Long-running text analysis is executed outside a database transaction, while this component
 * keeps every status/result update atomic and consistent.</p>
 */
@Service
public class AnalysisJobProcessor {

    private final AnalysisJobRepository analysisJobRepository;

    public AnalysisJobProcessor(@NonNull AnalysisJobRepository analysisJobRepository) {
        this.analysisJobRepository = analysisJobRepository;
    }

    /**
     * Marks an existing analysis job as running.
     *
     * @param jobId identifier of the job to update
     * @return {@code true} if the job exists and was updated, otherwise {@code false}
     */
    @Transactional
    public boolean markRunning(@NonNull Long jobId) {
        return analysisJobRepository.findById(jobId)
                .map(job -> {
                    job.setStatus(AnalysisStatus.RUNNING);
                    job.setStartedAt(Instant.now());
                    return true;
                })
                .orElse(false);
    }

    /**
     * Saves the successful analysis result and marks the job as completed in one transaction.
     *
     * @param jobId identifier of the job to complete
     * @param result calculated analysis result
     */
    @Transactional
    public void completeJob(@NonNull Long jobId, @NonNull AnalysisResult result) {
        AnalysisJobEntity job = findExistingJob(jobId);

        job.setStatus(AnalysisStatus.COMPLETED);
        job.setDirectory(result.analysisInfo().directory());
        job.setMinWordLength(result.analysisInfo().minWordLength());
        job.setTopCount(result.analysisInfo().topCount());
        job.setMode(result.analysisInfo().mode());
        job.setThreads(result.analysisInfo().threads());
        job.setProcessedFiles(result.analysisInfo().processedFiles());
        job.setExecutionTimeMs(result.analysisInfo().executionTimeMs());
        job.setFinishedAt(Instant.now());
        job.setErrorMessage(null);

        job.getWords().clear();
        result.words().forEach(word ->
                job.getWords().add(new WordCountEmbeddable(word.word(), word.count()))
        );

        job.getErrors().clear();
        result.errors().forEach(error ->
                job.getErrors().add(new FileErrorEmbeddable(error.file(), error.message()))
        );
    }

    /**
     * Saves the failure reason and marks the job as failed in one transaction.
     *
     * @param jobId identifier of the failed job
     * @param errorMessage human-readable failure reason
     */
    @Transactional
    public void failJob(@NonNull Long jobId, @NonNull String errorMessage) {
        AnalysisJobEntity job = findExistingJob(jobId);
        job.setStatus(AnalysisStatus.FAILED);
        job.setErrorMessage(errorMessage);
        job.setFinishedAt(Instant.now());
    }

    private AnalysisJobEntity findExistingJob(Long jobId) {
        return analysisJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Analysis job does not exist: " + jobId));
    }
}