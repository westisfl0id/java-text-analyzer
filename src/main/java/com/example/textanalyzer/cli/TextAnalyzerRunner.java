package com.example.textanalyzer.cli;

import com.example.textanalyzer.exception.BadArgumentsException;
import com.example.textanalyzer.io.ConsoleResultWriter;
import com.example.textanalyzer.io.ResultWriter;
import com.example.textanalyzer.io.StopWordsLoader;
import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.service.TextAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "text-analyzer.cli.enabled", havingValue = "true")
public class TextAnalyzerRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TextAnalyzerRunner.class);

    private final ArgsParser argsParser;
    private final HelpPrinter helpPrinter;
    private final StopWordsLoader stopWordsLoader;
    private final TextAnalysisService textAnalysisService;
    private final ConsoleResultWriter consoleResultWriter;
    private final ResultWriter jsonResultWriter;

    public TextAnalyzerRunner(
            ArgsParser argsParser,
            HelpPrinter helpPrinter,
            StopWordsLoader stopWordsLoader,
            TextAnalysisService textAnalysisService,
            ConsoleResultWriter consoleResultWriter,
            ResultWriter jsonResultWriter
    ) {
        this.argsParser = argsParser;
        this.helpPrinter = helpPrinter;
        this.stopWordsLoader = stopWordsLoader;
        this.textAnalysisService = textAnalysisService;
        this.consoleResultWriter = consoleResultWriter;
        this.jsonResultWriter = jsonResultWriter;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            CommandLineOptions options = argsParser.parse(args.getSourceArgs());

            if (options.help()) {
                helpPrinter.print();
                return;
            }

            Set<String> stopWords = options.stopWordsFile()
                    .map(stopWordsLoader::load)
                    .orElseGet(Collections::emptySet);

            AnalysisResult result = textAnalysisService.analyze(options, stopWords);
            writeResult(result, options.outputFile().orElse(null));
        } catch (BadArgumentsException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.err.println();
            helpPrinter.print();
        }
    }

    private void writeResult(AnalysisResult result, Path outputFile) {
        if (outputFile == null) {
            consoleResultWriter.write(result);
            return;
        }

        try {
            jsonResultWriter.write(result, outputFile);
            log.info("Result saved to {}", outputFile);
        } catch (IOException exception) {
            System.err.println("Error: cannot write JSON result to " + outputFile + ": " + exception.getMessage());
        }
    }
}
