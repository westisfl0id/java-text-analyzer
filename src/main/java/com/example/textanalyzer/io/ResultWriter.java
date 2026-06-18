package com.example.textanalyzer.io;

import com.example.textanalyzer.model.AnalysisResult;

import java.io.IOException;
import java.nio.file.Path;

public interface ResultWriter {

    void write(AnalysisResult result, Path outputFile) throws IOException;
}
