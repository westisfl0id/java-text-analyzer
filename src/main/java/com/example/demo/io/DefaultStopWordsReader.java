package com.example.demo.io;

import com.example.demo.exception.InvalidArgumentsException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DefaultStopWordsReader implements StopWordsReader {
    @Override
    public Set<String> readStopWords(Path path) {
        if (!Files.exists(path)) {
            throw new InvalidArgumentsException("Файл стоп-слов не найден: " + path);
        }

        if (!Files.isRegularFile(path)) {
            throw new InvalidArgumentsException("Путь к стоп-словам не является файлом: " + path);
        }

        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8)
                    .stream()
                    .map(line -> line.toLowerCase(Locale.ROOT).trim())
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new InvalidArgumentsException("Не удалось прочитать файл стоп-слов: " + path);
        }
    }
}
