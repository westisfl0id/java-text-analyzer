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

/**
 * Reads text files from the local file system.
 */
@Component
public class LocalTextFileReader implements TextFileReader {

    /**
     * Finds regular text files in the given directory.
     *
     * @param directory directory to scan
     * @return list of discovered text files
     */
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

    /**
     * Reads full file content as a string.
     *
     * @param file file to read
     * @return file content
     */
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
