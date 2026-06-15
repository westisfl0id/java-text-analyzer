package com.example.demo.cli;

import java.nio.file.Path;
import java.util.Optional;

public record CommandLineOptions(
        Path directory,
        int minWordLength,
        int topCount,
        Optional<Path> outputFile,
        Optional<Path> stopWordsFile,
        boolean help
) {
}
