# Text Analyzer

REST-сервис на Java 17 и Spring Boot 3 для анализа `.txt` файлов в указанной директории.

Приложение считает частоту слов, игнорирует регистр, знаки препинания и стоп-слова.
Результаты анализа сохраняются в PostgreSQL и доступны через REST API.

## Возможности

* запуск анализа через REST API;
* поддержка однопоточного и многопоточного режима анализа;
* обработка всех `.txt` файлов в указанной директории;
* подсчёт самых частых слов;
* фильтрация слов по минимальной длине;
* поддержка стоп-слов;
* сохранение результатов анализа в PostgreSQL;
* хранение ошибок обработки файлов;
* статусы анализа: `PENDING`, `RUNNING`, `COMPLETED`, `FAILED`;
* получение результата конкретного анализа по ID;
* получение списка всех прошлых анализов;
* Basic Auth через Spring Security;
* аудит запусков анализа: пользователь, время и параметры запуска.

## Технологии

* Java 17
* Spring Boot 3
* Spring Web
* Spring Data JPA
* Spring Security
* PostgreSQL
* Maven
* Jackson
* Lombok

## Структура проекта

```text id="kpqe3t"
src/main/java/com/example/textanalyzer
├── TextAnalyzerApplication.java
├── cli
│   ├── AnalysisMode.java
│   ├── ArgsParser.java
│   ├── CommandLineOptions.java
│   ├── HelpPrinter.java
│   └── TextAnalyzerRunner.java
├── config
│   └── SecurityConfig.java
├── exception
│   └── BadArgumentsException.java
├── execution
│   ├── FileAnalysisExecutor.java
│   ├── PooledFileAnalysisExecutor.java
│   ├── SingleFileAnalyzer.java
│   └── SingleThreadFileAnalysisExecutor.java
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
│   ├── FileAnalysisResult.java
│   ├── FileError.java
│   └── WordStat.java
├── persistence
│   ├── entity
│   │   ├── AnalysisJobEntity.java
│   │   ├── AnalysisStatus.java
│   │   ├── AuditLogEntity.java
│   │   ├── FileErrorEmbeddable.java
│   │   └── WordCountEmbeddable.java
│   └── repository
│       ├── AnalysisJobRepository.java
│       └── AuditLogRepository.java
├── rest
│   ├── controller
│   │   └── AnalysisController.java
│   ├── dto
│   │   ├── AnalysisInfoResponse.java
│   │   ├── AnalysisResultResponse.java
│   │   ├── AnalysisSummaryResponse.java
│   │   ├── AnalyzeRequest.java
│   │   ├── AnalyzeResponse.java
│   │   ├── FileErrorResponse.java
│   │   └── WordResponse.java
│   ├── exception
│   │   └── GlobalExceptionHandler.java
│   └── service
│       └── AnalysisService.java
├── service
│   ├── DefaultTextAnalysisService.java
│   └── TextAnalysisService.java
└── word
    ├── RegexWordExtractor.java
    └── WordExtractor.java
```

## Требования

* Java 17+
* Maven 3.8+
* PostgreSQL 14+

## Настройка PostgreSQL

Пример запуска PostgreSQL через Docker:

```bash id="enf0h9"
docker run --name text-analyzer-postgres \
  -e POSTGRES_DB=text_analyzer \
  -e POSTGRES_USER=text_user \
  -e POSTGRES_PASSWORD=text_password \
  -p 5432:5432 \
  -d postgres:16
```

Если контейнер уже создан:

```bash id="mv2i8l"
docker start text-analyzer-postgres
```

## Конфигурация

Основной конфигурационный файл:

```text id="cl0uzp"
src/main/resources/application.properties
```

Приложение использует переменные окружения для подключения к базе данных:

```properties id="v2g08y"
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Пример локального запуска с переменными окружения:

```bash id="k5fcys"
DB_URL=jdbc:postgresql://localhost:5432/text_analyzer \
DB_USERNAME=text_user \
DB_PASSWORD=text_password \
java -jar target/text-analyzer-1.0.0.jar
```

## Сборка

```bash id="sudnej"
mvn clean package
```

После сборки JAR-файл находится здесь:

```text id="m7qd2d"
target/text-analyzer-1.0.0.jar
```

## Запуск

```bash id="i8mpf2"
DB_URL=jdbc:postgresql://localhost:5432/text_analyzer \
DB_USERNAME=text_user \
DB_PASSWORD=text_password \
java -jar target/text-analyzer-1.0.0.jar
```

После запуска сервис доступен по адресу:

```text id="jnkpku"
http://localhost:8080
```

## Авторизация

API защищено через Spring Security Basic Auth.

Тестовые пользователи:

```text id="t2ds3k"
user / password
admin / admin
```

## REST API

### Запуск анализа

```http id="qptf19"
POST /api/analyze
```

Пример запроса:

```bash id="jlazcb"
curl -u user:password \
  -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "directory": "./texts",
    "minWordLength": 5,
    "topCount": 10,
    "mode": "multi",
    "threads": 4
  }'
```

Поля запроса:

| Поле            | Тип    | Описание                              |
| --------------- | ------ | ------------------------------------- |
| `directory`     | string | путь к директории с `.txt` файлами    |
| `minWordLength` | number | минимальная длина слова               |
| `topCount`      | number | количество самых частых слов          |
| `mode`          | string | режим анализа: `single` или `multi`   |
| `threads`       | number | количество потоков для `multi` режима |
| `stopWordsFile` | string | путь к файлу стоп-слов                |
| `stopWords`     | array  | список стоп-слов в теле запроса       |

Пример ответа:

```json id="vjyscn"
{
  "id": 1,
  "status": "PENDING"
}
```

### Получение результата анализа

```http id="suwlh4"
GET /api/results/{id}
```

Пример:

```bash id="izprg7"
curl -u user:password http://localhost:8080/api/results/1
```

Если анализ ещё выполняется, API возвращает статус:

```json id="n2wryr"
{
  "id": 1,
  "status": "RUNNING",
  "analysisInfo": null,
  "words": [],
  "errors": [],
  "errorMessage": null
}
```

Если анализ завершён, API возвращает результат:

```json id="oo2lh6"
{
  "id": 1,
  "status": "COMPLETED",
  "analysisInfo": {
    "directory": "./texts",
    "minWordLength": 5,
    "topCount": 10,
    "mode": "multi",
    "threads": 4,
    "processedFiles": 2,
    "executionTimeMs": 15
  },
  "words": [
    {
      "word": "development",
      "count": 4
    },
    {
      "word": "process",
      "count": 3
    }
  ],
  "errors": [],
  "errorMessage": null
}
```

### Получение списка всех анализов

```http id="hbwx0o"
GET /api/results
```

Пример:

```bash id="0xiv36"
curl -u user:password http://localhost:8080/api/results
```

## Проверка авторизации

Без Basic Auth запросы к API не выполняются:

```bash id="lkimh0"
curl http://localhost:8080/api/results
```

Ожидаемый результат:

```text id="qof5g6"
401 Unauthorized
```

С авторизацией:

```bash id="g63l73"
curl -u user:password http://localhost:8080/api/results
```

## Аудит

Каждый запуск анализа сохраняется в таблицу `audit_logs`.

В аудит записываются:

* имя пользователя;
* действие;
* время запуска;
* параметры анализа.

## Хранение данных

Результаты анализов сохраняются в PostgreSQL.

Основные таблицы:

* `analyses`
* `analysis_words`
* `analysis_errors`
* `audit_logs`

## Особенности реализации

* REST-слой отвечает только за HTTP API.
* Сервисный слой запускает анализ и сохраняет результат.
* Логика анализа текста из предыдущих этапов переиспользуется.
* Многопоточная обработка файлов вынесена в отдельный слой `execution`.
* Ошибки отдельных файлов не останавливают весь анализ.
* Если анализ ещё не завершён, API возвращает статус выполнения вместо результата.

## CLI-режим

CLI-runner отключён по умолчанию:

```properties id="fkzbbl"
text-analyzer.cli.enabled=false
```

Для запуска старого CLI-режима можно включить параметр:

```bash id="uz6k7w"
java -jar target/text-analyzer-1.0.0.jar \
  --text-analyzer.cli.enabled=true \
  --dir ./texts \
  --min-length 5 \
  --top 10 \
  --mode multi \
  --threads 4
```
