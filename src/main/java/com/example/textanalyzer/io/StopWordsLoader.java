package com.example.textanalyzer.io;

import com.example.textanalyzer.exception.BadArgumentsException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Contract for loading stop words from an external source.
 */
public interface StopWordsLoader {

    /**
     * Loads and normalizes stop words from the given file.
     *
     * @param file stop-words file
     * @return normalized stop words
     */
    Set<String> load(Path file);

    default void validateStopWordsFile(Path file) {
        if (file == null) {
            throw new BadArgumentsException("Stop words file is not specified");
        }
    }
}
