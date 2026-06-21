package com.example.textanalyzer.rest.controller;

import com.example.textanalyzer.rest.dto.AnalysisResultResponse;
import com.example.textanalyzer.rest.dto.AnalysisSummaryResponse;
import com.example.textanalyzer.rest.dto.AnalyzeRequest;
import com.example.textanalyzer.rest.dto.AnalyzeResponse;
import com.example.textanalyzer.rest.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            Authentication authentication
    ) {
        AnalyzeResponse response = analysisService.startAnalysis(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @GetMapping("/results/{id}")
    public AnalysisResultResponse getResult(@PathVariable Long id) {
        return analysisService.getResult(id);
    }

    @GetMapping("/results")
    public List<AnalysisSummaryResponse> getAllResults() {
        return analysisService.getAllResults();
    }
}