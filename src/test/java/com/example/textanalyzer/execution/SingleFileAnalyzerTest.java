package com.example.textanalyzer.execution;

import com.example.textanalyzer.io.LocalTextFileReader;
import com.example.textanalyzer.model.FileAnalysisResult;
import com.example.textanalyzer.word.RegexWordExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class SingleFileAnalyzerTest {

    @TempDir
    Path tempDir;

    @Test
    void analyzesReadableFileAndAppliesFilters() throws Exception {
        Path file = tempDir.resolve("input.txt");
        Files.writeString(file, "Java java spring code and code");

        SingleFileAnalyzer analyzer = new SingleFileAnalyzer(
                new LocalTextFileReader(),
                new RegexWordExtractor()
        );

        FileAnalysisResult result = analyzer.analyze(file, 4, Set.of("and"));

        assertThat(result.successful()).isTrue();
        assertThat(result.wordCounts())
                .containsEntry("java", 2L)
                .containsEntry("code", 2L)
                .containsEntry("spring", 1L)
                .doesNotContainKey("and");
    }

    @Test
    void returnsFileErrorWhenFileCannotBeRead() {
        SingleFileAnalyzer analyzer = new SingleFileAnalyzer(
                new LocalTextFileReader(),
                new RegexWordExtractor()
        );

        FileAnalysisResult result = analyzer.analyze(tempDir.resolve("missing.txt"), 3, Set.of());

        assertThat(result.successful()).isFalse();
        assertThat(result.error()).isPresent();
        assertThat(result.wordCounts()).isEmpty();
    }
}