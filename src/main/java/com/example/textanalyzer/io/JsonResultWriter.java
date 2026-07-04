package com.example.textanalyzer.io;

import com.example.textanalyzer.model.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes analysis results as JSON.
 */
@Component
public class JsonResultWriter implements ResultWriter {

    private final ObjectMapper objectMapper;

    /**
     * Creates a JSON result writer.
     *
     * @param objectMapper mapper used to serialize results
     */
    public JsonResultWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Serializes the result and writes it to the provided output file.
     *
     * @param result analysis result to serialize
     * @param outputFile target JSON file
     */
    @Override
    public void write(AnalysisResult result, Path outputFile) throws IOException {
        Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValue(outputFile.toFile(), result);
    }
}
