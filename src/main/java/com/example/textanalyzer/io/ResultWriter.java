package com.example.textanalyzer.io;

import com.example.textanalyzer.model.AnalysisResult;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Contract for writing analysis results to an output destination.
 */
public interface ResultWriter {

    /**
     * Writes the provided analysis result.
     *
     * @param result analysis result to write
     * @param outputFile optional target file
     */
    void write(AnalysisResult result, Path outputFile) throws IOException;
}
