package com.example.textanalyzer.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TextFileReader {

    List<Path> findTextFiles(Path directory) throws IOException;

    String readFile(Path file) throws IOException;
}
