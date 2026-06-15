# Java Text Analyzer

Консольное приложение на **Java 17** и **Spring Boot**, которое анализирует текстовые файлы в указанной папке и выводит самые часто встречающиеся слова.

## Описание

Приложение принимает путь к папке с `.txt` файлами, минимальную длину слова и количество самых популярных слов для вывода.

При анализе приложение:

* читает все `.txt` файлы из указанной папки;
* извлекает слова с помощью регулярных выражений;
* игнорирует регистр слов;
* игнорирует знаки препинания;
* игнорирует стоп-слова, если указан файл со стоп-словами;
* считает количество вхождений каждого слова;
* выводит результат в консоль или сохраняет его в JSON-файл.

## Технологии

* Java 17
* Spring Boot
* Maven
* Jackson
* SLF4J / Logback

## Структура проекта

```text
src/main/java/com/example/demo
├── cli
│   ├── AnalyzerCommandLineRunner.java
│   ├── CommandLineOptions.java
│   └── CommandLineOptionsParser.java
├── exception
│   └── InvalidArgumentsException.java
├── io
│   ├── DefaultStopWordsReader.java
│   ├── DefaultTextFileReader.java
│   ├── JsonResultWriter.java
│   ├── StopWordsReader.java
│   └── TextFileReader.java
├── model
│   ├── AnalysisInfo.java
│   ├── AnalysisResult.java
│   ├── FileError.java
│   └── WordStat.java
├── service
│   ├── DefaultTextAnalysisService.java
│   └── TextAnalysisService.java
├── word
│   ├── RegexWordExtractor.java
│   └── WordExtractor.java
└── DemoApplication.java
```

## Параметры запуска

| Параметр       | Описание                                    | Обязательный |
| -------------- | ------------------------------------------- | ------------ |
| `--dir`        | Путь к папке с `.txt` файлами               | Да           |
| `--min-length` | Минимальная длина слова                     | Да           |
| `--top`        | Количество самых частых слов                | Да           |
| `--output`     | Путь к JSON-файлу для сохранения результата | Нет          |
| `--stopwords`  | Путь к файлу со стоп-словами                | Нет          |
| `--help`       | Вывод справки                               | Нет          |

## Пример запуска из IntelliJ IDEA

В настройках запуска нужно указать **Program arguments**:

```bash
--dir ./texts --min-length 5 --top 10
```

Папка `texts` должна находиться в корне проекта.

Пример файла:

```text
texts/test.txt
```

Содержимое файла:

```text
Development process is important.
Development and engineering process.
Java development process.
```

## Пример вывода в консоль

```text
1. development — 3
2. process — 3
3. engineering — 1
4. important — 1
```

## Запуск через Maven

Сначала нужно собрать проект:

```bash
./mvnw clean package
```

Или, если Maven установлен глобально:

```bash
mvn clean package
```

После сборки приложение можно запустить через jar-файл:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar --dir ./texts --min-length 5 --top 10
```

Если название jar-файла отличается, его можно посмотреть в папке `target`.

## Запуск с JSON-выводом

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar --dir ./texts --min-length 5 --top 10 --output ./results.json
```

Пример JSON-результата:

```json
{
  "analysisInfo" : {
    "directory" : "./texts",
    "minWordLength" : 5,
    "topCount" : 10
  },
  "words" : [ {
    "word" : "development",
    "count" : 3
  }, {
    "word" : "process",
    "count" : 3
  }, {
    "word" : "engineering",
    "count" : 1
  }, {
    "word" : "important",
    "count" : 1
  } ],
  "errors" : [ ]
}
```

## Запуск со стоп-словами

Создать файл:

```text
stop.txt
```

Пример содержимого:

```text
development
process
```

Запуск:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar --dir ./texts --min-length 5 --top 10 --stopwords ./stop.txt
```

В этом случае слова `development` и `process` не будут учитываться при подсчёте.

## Справка

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar --help
```

## Обработка ошибок

Приложение обрабатывает следующие ситуации:

* не указаны обязательные параметры;
* указан неверный путь к папке;
* путь не является папкой;
* в папке нет `.txt` файлов;
* файл пустой;
* файл недоступен для чтения;
* неверно указаны параметры запуска.

