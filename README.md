# Text Analyzer

Console application on Java 17 and Spring Boot 3 for analyzing `.txt` files in a directory.

The application counts words, ignores punctuation, word case, and optional stop words, then outputs the most frequent words.

## Requirements

- Java 17+
- Maven 3.8+

## Build

```bash
mvn clean package
```

After building, the executable JAR will be here:

```text
target/text-analyzer-1.0.0.jar
```

## Parameters

Required:

- `--dir` — path to a directory with `.txt` files
- `--min-length` — minimum word length
- `--top` — number of most frequent words to output

Optional:

- `--output` — path to JSON output file
- `--stopwords` — path to a file with stop words
- `--help` — show help

## Usage examples

Output to console:

```bash
java -jar target/text-analyzer-1.0.0.jar --dir ./texts --min-length 5 --top 10
```

Output to JSON:

```bash
java -jar target/text-analyzer-1.0.0.jar --dir ./texts --min-length 5 --top 10 --stopwords ./stop.txt --output ./results.json
```

Help:

```bash
java -jar target/text-analyzer-1.0.0.jar --help
```

## Stop words file example

```text
the
and
or
in
on
```

## Console output example

```text
1. development — 112
2. process — 97
3. engineering — 83
```

## JSON output example

```json
{
  "analysisInfo": {
    "directory": "./texts",
    "minWordLength": 5,
    "topCount": 10
  },
  "words": [
    {
      "word": "development",
      "count": 112
    },
    {
      "word": "process",
      "count": 97
    },
    {
      "word": "engineering",
      "count": 83
    }
  ],
  "errors": [
    {
      "file": "broken.txt",
      "message": "Access denied"
    }
  ]
}
```

## Project structure

```text
src/main/java/com/example/textanalyzer
├── TextAnalyzerApplication.java
├── cli
│   ├── ArgsParser.java
│   ├── CommandLineOptions.java
│   ├── HelpPrinter.java
│   └── TextAnalyzerRunner.java
├── exception
│   └── BadArgumentsException.java
├── io
│   ├── ConsoleResultWriter.java
│   ├── FileStopWordsLoader.java
│   ├── JsonResultWriter.java
│   ├── LocalTextFileReader.java
│   ├── ResultWriter.java
│   ├── StopWordsLoader.java
│   └── TextFileReader.java
├── model
│   ├── AnalysisInfo.java
│   ├── AnalysisResult.java
│   ├── FileError.java
│   └── WordStat.java
├── service
│   ├── DefaultTextAnalysisService.java
│   └── TextAnalysisService.java
└── word
    ├── RegexWordExtractor.java
    └── WordExtractor.java
```

## Notes

- Files are searched recursively in the selected directory.
- Only files ending with `.txt` are analyzed.
- Words are extracted by regular expression.
- Word case is ignored.
- Punctuation is ignored.
- The result is sorted by count descending, then alphabetically.
- If one file cannot be read, the application continues analyzing other files and adds the file error to the result.
