package com.example.demo.cli;

import com.example.demo.exception.InvalidArgumentsException;
import com.example.demo.io.JsonResultWriter;
import com.example.demo.io.StopWordsReader;
import com.example.demo.model.AnalysisResult;
import com.example.demo.model.WordStat;
import com.example.demo.service.TextAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Set;

@Component
public class AnalyzerCommandLineRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AnalyzerCommandLineRunner.class);

    private final CommandLineOptionsParser optionsParser;
    private final StopWordsReader stopWordsReader;
    private final TextAnalysisService textAnalysisService;
    private final JsonResultWriter jsonResultWriter;

    public AnalyzerCommandLineRunner(
            CommandLineOptionsParser optionsParser,
            StopWordsReader stopWordsReader,
            TextAnalysisService textAnalysisService,
            JsonResultWriter jsonResultWriter
    ) {
        this.optionsParser = optionsParser;
        this.stopWordsReader = stopWordsReader;
        this.textAnalysisService = textAnalysisService;
        this.jsonResultWriter = jsonResultWriter;
    }

    @Override
    public void run(String... args) {
        try {
            CommandLineOptions options = optionsParser.parse(args);

            if (options.help()) {
                System.out.println(optionsParser.getHelpText());
                return;
            }

            Set<String> stopWords = options.stopWordsFile()
                    .map(stopWordsReader::readStopWords)
                    .orElse(Set.of());

            AnalysisResult result = textAnalysisService.analyze(options, stopWords);

            if (options.outputFile().isPresent()) {
                Path outputPath = options.outputFile().get();
                jsonResultWriter.write(result, outputPath);
                System.out.println("Результат сохранён в файл: " + outputPath);
            } else {
                printToConsole(result);
            }

        } catch (InvalidArgumentsException e) {
            System.err.println("Ошибка параметров запуска: " + e.getMessage());
            System.err.println();
            System.err.println(optionsParser.getHelpText());
            log.warn("Invalid arguments: {}", e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка выполнения приложения: " + e.getMessage());
            log.error("Application error", e);
        }
    }

    private void printToConsole(AnalysisResult result) {
        if (result.words().isEmpty()) {
            System.out.println("Слова не найдены.");
            return;
        }

        int index = 1;

        for (WordStat wordStat : result.words()) {
            System.out.printf("%d. %s — %d%n", index, wordStat.word(), wordStat.count());
            index++;
        }

        if (!result.errors().isEmpty()) {
            System.out.println();
            System.out.println("Ошибки при обработке файлов:");

            result.errors().forEach(error ->
                    System.out.printf("- %s: %s%n", error.file(), error.message())
            );
        }
    }
}
