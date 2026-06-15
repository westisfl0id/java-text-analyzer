package com.example.demo.io;

import java.io.IOException;
import java.nio.file.Path;

public interface TextFileReader {
    String read(Path path) throws IOException;
}
