package com.example.demo.cli;

import com.example.demo.exception.InvalidArgumentsException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class CommandLineOptionsParser {

    private static final Set<String> ALLOWED_ARGUMENTS = Set.of(
            "--dir",
            "--min-length",
            "--top",
            "--output",
            "--stopwords"
    );

    public CommandLineOptions parse(String[] args) {
        if (containsHelp(args)) {
            return new CommandLineOptions(
                    null,
                    0,
                    0,
                    Optional.empty(),
                    Optional.empty(),
                    true
            );
        }

        Map<String, String> arguments = parseArguments(args);

        Path directory = getRequiredPath(arguments, "--dir");
        int minLength = getRequiredPositiveInt(arguments, "--min-length");
        int top = getRequiredPositiveInt(arguments, "--top");

        Optional<Path> output = getOptionalPath(arguments, "--output");
        Optional<Path> stopWords = getOptionalPath(arguments, "--stopwords");

        return new CommandLineOptions(
                directory,
                minLength,
                top,
                output,
                stopWords,
                false
        );
    }

    public String getHelpText() {
        return """
                Text Analyzer
                
                Обязательные параметры:
                  --dir <path>             путь к папке с .txt файлами
                  --min-length <number>    минимальная длина слова
                  --top <number>           количество наиболее частых слов
                
                Опциональные параметры:
                  --output <path>          путь к JSON-файлу для сохранения результата
                  --stopwords <path>       путь к файлу со стоп-словами
                  --help                   показать справку
                
                Примеры:
                  java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10
                
                  java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10 --stopwords ./stop.txt --output ./results.json
                """;
    }

    private boolean containsHelp(String[] args) {
        for (String arg : args) {
            if ("--help".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> parseArguments(String[] args) {
        Map<String, String> result = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String current = args[i];

            if (!current.startsWith("--")) {
                throw new InvalidArgumentsException("Неизвестный параметр: " + current);
            }

            if (!ALLOWED_ARGUMENTS.contains(current)) {
                throw new InvalidArgumentsException("Неизвестный параметр: " + current);
            }

            if (i + 1 >= args.length) {
                throw new InvalidArgumentsException("Для параметра " + current + " не указано значение");
            }

            String value = args[i + 1];

            if (value.startsWith("--")) {
                throw new InvalidArgumentsException("Для параметра " + current + " не указано значение");
            }

            result.put(current, value);
            i++;
        }

        return result;
    }

    private Path getRequiredPath(Map<String, String> arguments, String key) {
        String value = arguments.remove(key);

        if (value == null || value.isBlank()) {
            throw new InvalidArgumentsException("Обязательный параметр отсутствует: " + key);
        }

        return Path.of(value);
    }

    private int getRequiredPositiveInt(Map<String, String> arguments, String key) {
        String value = arguments.remove(key);

        if (value == null || value.isBlank()) {
            throw new InvalidArgumentsException("Обязательный параметр отсутствует: " + key);
        }

        try {
            int parsed = Integer.parseInt(value);

            if (parsed <= 0) {
                throw new InvalidArgumentsException("Параметр " + key + " должен быть положительным числом");
            }

            return parsed;
        } catch (NumberFormatException e) {
            throw new InvalidArgumentsException("Параметр " + key + " должен быть числом");
        }
    }

    private Optional<Path> getOptionalPath(Map<String, String> arguments, String key) {
        String value = arguments.remove(key);

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(Path.of(value));
    }
}