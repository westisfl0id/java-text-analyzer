package com.example.demo.io;

import java.nio.file.Path;
import java.util.Set;

public interface StopWordsReader {
    Set<String> readStopWords(Path path);
}
