package com.example.textanalyzer.service;

import com.example.textanalyzer.cli.AnalysisMode;
import com.example.textanalyzer.cli.CommandLineOptions;
import com.example.textanalyzer.execution.PooledFileAnalysisExecutor;
import com.example.textanalyzer.execution.SingleFileAnalyzer;
import com.example.textanalyzer.execution.SingleThreadFileAnalysisExecutor;
import com.example.textanalyzer.io.LocalTextFileReader;
import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.word.RegexWordExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultTextAnalysisServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void aggregatesWordsFromAllTextFilesAndSortsTopResult() throws Exception {
        Files.writeString(tempDir.resolve("first.txt"), "alpha beta beta gamma");
        Files.writeString(tempDir.resolve("second.txt"), "beta alpha delta");
        Files.writeString(tempDir.resolve("ignored.md"), "beta beta beta");

        DefaultTextAnalysisService service = createService();

        AnalysisResult result = service.analyze(new CommandLineOptions(
                tempDir,
                4,
                3,
                Optional.empty(),
                Optional.empty(),
                AnalysisMode.SINGLE,
                1,
                false
        ), Set.of("gamma"));

        assertThat(result.analysisInfo().processedFiles()).isEqualTo(2);
        assertThat(result.words())
                .extracting("word")
                .containsExactly("beta", "alpha", "delta");
        assertThat(result.words())
                .extracting("count")
                .containsExactly(3L, 2L, 1L);
        assertThat(result.errors()).isEmpty();
    }

    private DefaultTextAnalysisService createService() {
        LocalTextFileReader reader = new LocalTextFileReader();
        SingleFileAnalyzer analyzer = new SingleFileAnalyzer(reader, new RegexWordExtractor());
        return new DefaultTextAnalysisService(
                reader,
                new SingleThreadFileAnalysisExecutor(analyzer),
                new PooledFileAnalysisExecutor(analyzer)
        );
    }
}