package com.example.textanalyzer.io;

import com.example.textanalyzer.exception.BadArgumentsException;

import java.nio.file.Path;
import java.util.Set;

public interface StopWordsLoader {

    Set<String> load(Path file);

    default void validateStopWordsFile(Path file) {
        if (file == null) {
            throw new BadArgumentsException("Stop words file is not specified");
        }
    }
}
