package com.example.textanalyzer.io;

import com.example.textanalyzer.exception.BadArgumentsException;
import com.example.textanalyzer.word.WordExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileStopWordsLoader implements StopWordsLoader {

    private final WordExtractor wordExtractor;

    public FileStopWordsLoader(WordExtractor wordExtractor) {
        this.wordExtractor = wordExtractor;
    }

    @Override
    public Set<String> load(Path file) {
        validateStopWordsFile(file);
        validateReadableRegularFile(file);

        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return lines
                    .flatMap(line -> wordExtractor.extractWords(line).stream())
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException exception) {
            throw new BadArgumentsException("Cannot read stop words file: " + file);
        }
    }

    private void validateReadableRegularFile(Path file) {
        if (!Files.exists(file)) {
            throw new BadArgumentsException("Stop words file does not exist: " + file);
        }
        if (!Files.isRegularFile(file)) {
            throw new BadArgumentsException("Stop words path is not a file: " + file);
        }
        if (!Files.isReadable(file)) {
            throw new BadArgumentsException("Stop words file is not readable: " + file);
        }
    }
}
