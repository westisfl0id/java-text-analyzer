package com.example.demo.model;

import java.util.List;

public record AnalysisResult (
        AnalysisInfo analysisInfo,
        List<WordStat> words,
        List<FileError> errors
){
}
