package com.example.textanalyzer.cli;

import org.springframework.stereotype.Component;

@Component
public class HelpPrinter {

    public void print() {
        System.out.println("""
                Usage:
                  java -jar text-analyzer.jar --dir <path> --min-length <number> --top <number> [--mode single|multi] [--threads <number>] [--output <file.json>] [--stopwords <file>] [--help]

                Required parameters:
                  --dir          Path to a directory with .txt files
                  --min-length   Minimum word length to count
                  --top          Number of most frequent words to output

                Optional parameters:
                  --mode         Analysis mode: single or multi. Default: single
                  --threads      Worker threads for multi mode. Default: 2
                  --output       Path to JSON file for saving result
                  --stopwords    Path to file with stop words
                  --help         Show this help message

                Examples:
                  java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10
                  java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10 --mode multi --threads 4
                  java -jar text-analyzer.jar --dir ./texts --min-length 5 --top 10 --mode multi --threads 4 --stopwords ./stop.txt --output ./results.json
                """);
    }
}