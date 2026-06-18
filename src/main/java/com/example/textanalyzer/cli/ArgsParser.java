package com.example.textanalyzer.cli;

import com.example.textanalyzer.exception.BadArgumentsException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class ArgsParser {

    private static final int DEFAULT_THREADS = 2;

    private static final Set<String> KNOWN_PARAMETERS = Set.of(
            "dir",
            "min-length",
            "top",
            "output",
            "stopwords",
            "mode",
            "threads",
            "help"
    );

    public CommandLineOptions parse(String[] args) {
        Map<String, String> parameters = new LinkedHashMap<>();

        for (int i = 0; i < args.length; i++) {
            String token = args[i];

            if (!token.startsWith("--")) {
                throw new BadArgumentsException("Unexpected argument: " + token);
            }

            String raw = token.substring(2);
            String name;
            String value;

            int equalsIndex = raw.indexOf('=');
            if (equalsIndex >= 0) {
                name = raw.substring(0, equalsIndex);
                value = raw.substring(equalsIndex + 1);
            } else {
                name = raw;

                if ("help".equals(name)) {
                    value = "true";
                } else {
                    if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                        throw new BadArgumentsException("Missing value for parameter --" + name);
                    }
                    value = args[++i];
                }
            }

            validateParameterName(name);
            validateNotDuplicated(parameters, name);
            parameters.put(name, value);
        }

        if (parameters.containsKey("help")) {
            return CommandLineOptions.helpOptions();
        }

        String directory = required(parameters, "dir");
        int minWordLength = positiveInteger(required(parameters, "min-length"), "min-length");
        int topCount = positiveInteger(required(parameters, "top"), "top");

        AnalysisMode mode = analysisMode(parameters.getOrDefault("mode", "single"));

        int threads = positiveInteger(
                parameters.getOrDefault("threads", String.valueOf(DEFAULT_THREADS)),
                "threads"
        );

        return new CommandLineOptions(
                Path.of(directory).normalize(),
                minWordLength,
                topCount,
                optionalPath(parameters, "output"),
                optionalPath(parameters, "stopwords"),
                mode,
                threads,
                false
        );
    }

    private void validateParameterName(String name) {
        if (name.isBlank() || !KNOWN_PARAMETERS.contains(name)) {
            throw new BadArgumentsException("Unknown parameter --" + name);
        }
    }

    private void validateNotDuplicated(Map<String, String> parameters, String name) {
        if (parameters.containsKey(name)) {
            throw new BadArgumentsException("Duplicated parameter --" + name);
        }
    }

    private String required(Map<String, String> parameters, String name) {
        String value = parameters.get(name);
        if (value == null || value.isBlank()) {
            throw new BadArgumentsException("Required parameter is missing: --" + name);
        }
        return value;
    }

    private Optional<Path> optionalPath(Map<String, String> parameters, String name) {
        String value = parameters.get(name);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Path.of(value).normalize());
    }

    private int positiveInteger(String value, String parameterName) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1) {
                throw new BadArgumentsException("Parameter --" + parameterName + " must be greater than 0");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new BadArgumentsException("Parameter --" + parameterName + " must be a number");
        }
    }

    private AnalysisMode analysisMode(String value) {
        if (value == null || value.isBlank()) {
            return AnalysisMode.SINGLE;
        }

        try {
            return AnalysisMode.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new BadArgumentsException("Parameter --mode must be either single or multi");
        }
    }
}