package com.example.textanalyzer.io;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Component
public class LocalTextFileReader implements TextFileReader {

    @Override
    public List<Path> findTextFiles(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isTextFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        }
    }

    @Override
    public String readFile(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    private boolean isTextFile(Path path) {
        return path.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT)
                .endsWith(".txt");
    }
}
