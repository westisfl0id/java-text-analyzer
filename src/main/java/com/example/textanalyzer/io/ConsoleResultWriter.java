package com.example.textanalyzer.io;

import com.example.textanalyzer.model.AnalysisInfo;
import com.example.textanalyzer.model.AnalysisResult;
import com.example.textanalyzer.model.FileError;
import com.example.textanalyzer.model.WordStat;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ConsoleResultWriter {

    public void write(AnalysisResult result) {
        AnalysisInfo info = result.analysisInfo();

        System.out.printf(
                "Mode: %s (%d workers)%n%n",
                info.mode().toUpperCase(Locale.ROOT),
                info.threads()
        );

        System.out.printf(
                "Processed %d files in %d ms%n%n",
                info.processedFiles(),
                info.executionTimeMs()
        );

        System.out.printf(
                "Top %d words (min length = %d):%n%n",
                info.topCount(),
                info.minWordLength()
        );

        if (result.words().isEmpty()) {
            System.out.println("No words found for the specified parameters.");
        }

        int position = 1;

        for (WordStat wordStat : result.words()) {
            System.out.printf("%d. %s — %d%n", position++, wordStat.word(), wordStat.count());
        }

        if (!result.errors().isEmpty()) {
            System.out.println();
            System.out.println("Errors:");

            for (FileError error : result.errors()) {
                System.out.printf("- %s: %s%n", error.file(), error.message());
            }
        }
    }
}