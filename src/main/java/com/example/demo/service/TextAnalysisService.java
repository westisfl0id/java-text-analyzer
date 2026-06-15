package com.example.demo.service;

import com.example.demo.cli.CommandLineOptions;
import com.example.demo.model.AnalysisResult;

import java.util.Set;

public interface TextAnalysisService {
    AnalysisResult analyze(CommandLineOptions options, Set<String> stopWords);
}
