package com.example.textanalyzer.cli;

import com.example.textanalyzer.exception.BadArgumentsException;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgsParserTest {

    private final ArgsParser argsParser = new ArgsParser();

    @Test
    void parsesRequiredAndOptionalArguments() {
        CommandLineOptions options = argsParser.parse(new String[]{
                "--dir", "texts",
                "--min-length=4",
                "--top", "3",
                "--mode", "multi",
                "--threads", "5",
                "--stopwords", "stop.txt",
                "--output", "result.json"
        });

        assertThat(options.directory()).isEqualTo(Path.of("texts").normalize());
        assertThat(options.minWordLength()).isEqualTo(4);
        assertThat(options.topCount()).isEqualTo(3);
        assertThat(options.mode()).isEqualTo(AnalysisMode.MULTI);
        assertThat(options.threads()).isEqualTo(5);
        assertThat(options.effectiveThreads()).isEqualTo(5);
        assertThat(options.stopWordsFile()).contains(Path.of("stop.txt").normalize());
        assertThat(options.outputFile()).contains(Path.of("result.json").normalize());
    }

    @Test
    void returnsHelpOptionsWhenHelpFlagIsPresent() {
        CommandLineOptions options = argsParser.parse(new String[]{"--help"});

        assertThat(options.help()).isTrue();
    }

    @Test
    void rejectsUnknownArguments() {
        assertThatThrownBy(() -> argsParser.parse(new String[]{"--bad", "value"}))
                .isInstanceOf(BadArgumentsException.class)
                .hasMessageContaining("Unknown parameter");
    }

    @Test
    void rejectsNonPositiveNumbers() {
        assertThatThrownBy(() -> argsParser.parse(new String[]{
                "--dir", "texts",
                "--min-length", "0",
                "--top", "10"
        }))
                .isInstanceOf(BadArgumentsException.class)
                .hasMessageContaining("greater than 0");
    }
}