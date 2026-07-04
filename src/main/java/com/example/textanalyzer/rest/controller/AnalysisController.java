package com.example.textanalyzer.rest.controller;

import com.example.textanalyzer.rest.dto.AnalysisResultResponse;
import com.example.textanalyzer.rest.dto.AnalysisSummaryResponse;
import com.example.textanalyzer.rest.dto.AnalyzeRequest;
import com.example.textanalyzer.rest.dto.AnalyzeResponse;
import com.example.textanalyzer.rest.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * REST controller for creating text-analysis jobs and reading their results.
 */
@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(@NonNull AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Accepts a new analysis request and returns the created job identifier immediately.
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(
            @Valid @RequestBody @NonNull AnalyzeRequest request,
            @NonNull Authentication authentication
    ) {
        AnalyzeResponse response = analysisService.startAnalysis(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    /**
     * Returns detailed information for one analysis job.
     */
    @GetMapping("/results/{id}")
    public AnalysisResultResponse getResult(@PathVariable @NonNull Long id) {
        return analysisService.getResult(id);
    }

    /**
     * Returns compact summaries of all analysis jobs.
     */
    @GetMapping("/results")
    public List<AnalysisSummaryResponse> getAllResults() {
        return analysisService.getAllResults();
    }
}