package com.example.textanalyzer.io;

import com.example.textanalyzer.model.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class JsonResultWriter implements ResultWriter {

    private final ObjectMapper objectMapper;

    public JsonResultWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
