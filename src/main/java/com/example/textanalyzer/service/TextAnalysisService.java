package com.example.textanalyzer.service;

import com.example.textanalyzer.cli.CommandLineOptions;
import com.example.textanalyzer.model.AnalysisResult;

import java.util.Set;

public interface TextAnalysisService {

    AnalysisResult analyze(CommandLineOptions options, Set<String> stopWords);
}
