package com.example.textanalyzer.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Contract for reading text files and discovering input files.
 */
public interface TextFileReader {

    /**
     * Finds text files inside the given directory.
     *
     * @param directory source directory
     * @return discovered text files
     */
    List<Path> findTextFiles(Path directory) throws IOException;

    /**
     * Reads full text content from a file.
     *
     * @param file file to read
     * @return file content
     */
    String readFile(Path file) throws IOException;
}
