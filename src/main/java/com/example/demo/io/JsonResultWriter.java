package com.example.demo.io;


import com.example.demo.model.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class JsonResultWriter {
    private final ObjectMapper objectMapper;

    public JsonResultWriter() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void write(AnalysisResult result, Path outputPath) {
        try {
            Path parent = outputPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            objectMapper.writeValue(outputPath.toFile(), result);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать JSON-файл: " + outputPath, e);
        }
    }
}
