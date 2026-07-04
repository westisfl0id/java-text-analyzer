# Text Analyzer
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Maven](https://img.shields.io/badge/Maven-3-red)

REST-сервис на Java 17 и Spring Boot 3 для анализа текстовых файлов.

Приложение анализирует все `.txt` файлы в указанной директории, подсчитывает частоту слов, игнорирует регистр, знаки препинания и стоп-слова, сохраняет результаты анализа в PostgreSQL и предоставляет доступ к ним через REST API.

---

# Возможности

- анализ всех `.txt` файлов в директории;
- однопоточный и многопоточный режим работы;
- подсчет наиболее часто встречающихся слов;
- фильтрация слов по минимальной длине;
- использование стоп-слов;
- сохранение результатов анализа в PostgreSQL;
- хранение ошибок обработки отдельных файлов;
- отслеживание статуса выполнения анализа;
- получение результата анализа по идентификатору;
- получение списка всех анализов;
- Basic Authentication через Spring Security;
- аудит запусков анализа.

---

# Используемые технологии

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Flyway
- Maven
- Jackson
- Lombok

---

# Структура проекта

```text
src
├── main
│   ├── java
│   │   └── com/example/textanalyzer
│   │       ├── cli
│   │       ├── config
│   │       ├── execution
│   │       ├── io
│   │       ├── model
│   │       ├── persistence
│   │       ├── rest
│   │       ├── service
│   │       ├── word
│   │       └── TextAnalyzerApplication.java
│   └── resources
│       ├── application.properties
│       └── db
│           └── migration
```

Полная структура проекта включает:

- REST API;
- сервисный слой;
- слой анализа текста;
- слой доступа к данным;
- конфигурацию безопасности;
- CLI-модуль;
- миграции Flyway.

---

# Требования

- Java 17+
- Maven 3.8+
- Docker
- Docker Compose

---


# Запуск PostgreSQL

Перед первым запуском создайте файл `.env` на основе шаблона:

```bash
cp .env.example .env
```

После этого запустите PostgreSQL:

```bash
docker compose up -d
```

Проверить, что контейнер успешно запущен, можно командой:

```bash
docker ps
```

---

# Конфигурация

Основной конфигурационный файл:

```text
src/main/resources/application.properties
```

Для подключения используются переменные окружения:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/text_analyzer}
spring.datasource.username=${DB_USERNAME:text_user}
spring.datasource.password=${DB_PASSWORD:text_password}
```

Пример файла `.env`:

```env
DB_URL=jdbc:postgresql://localhost:5432/text_analyzer
DB_USERNAME=text_user
DB_PASSWORD=text_password

TEXT_ANALYZER_SECURITY_USER_USERNAME=user
TEXT_ANALYZER_SECURITY_USER_PASSWORD=password

TEXT_ANALYZER_SECURITY_ADMIN_USERNAME=admin
TEXT_ANALYZER_SECURITY_ADMIN_PASSWORD=admin
```

Все параметры подключения автоматически считываются Spring Boot при запуске приложения.

---

# Миграции базы данных

Для управления структурой базы данных используется Flyway.

Все SQL-миграции располагаются в каталоге:

```text
src/main/resources/db/migration
```

При запуске приложения Flyway автоматически применяет отсутствующие миграции.

Использование Flyway позволяет:

- хранить структуру базы данных в системе контроля версий;
- автоматически создавать таблицы;
- автоматически создавать индексы;
- отказаться от использования Hibernate DDL;
- безопасно изменять структуру базы данных.

---

# Полный запуск проекта

- Создать файл .env.
- cp .env.example .env
- Запустить PostgreSQL.
- docker compose up -d
 - Собрать проект.
mvn clean package
Запустить приложение.
java -jar target/text-analyzer-1.0.0.jar
После запуска открыть:
http://localhost:8080


# Сборка проекта

```bash
mvn clean package
```

После успешной сборки будет создан файл:

```text
target/text-analyzer-1.0.0.jar
```

---

# Полный запуск проекта

1. Создать файл `.env`.

```bash
cp .env.example .env
```

2. Запустить PostgreSQL.

```bash
docker compose up -d
```

3. Собрать проект.

```bash
mvn clean package
```

4. Запустить приложение.

```bash
java -jar target/text-analyzer-1.0.0.jar
```

5. После запуска открыть:

```text
http://localhost:8080
```

---

# Авторизация

API защищено Spring Security Basic Authentication.

Тестовые пользователи:

| Логин | Пароль |
|-------|--------|
| user | user   |
| admin | admin  |

---

# REST API

Все запросы требуют Basic Authentication.

## Запуск анализа

```
POST /api/analyze
```

Пример запроса:

```bash
curl -u user:password \
-X POST http://localhost:8080/api/analyze \
-H "Content-Type: application/json" \
-d '{
    "directory":"./texts",
    "minWordLength":5,
    "topCount":10,
    "mode":"multi",
    "threads":4
}'
```

Поля запроса:

| Поле | Тип | Описание |
|------|-----|----------|
| directory | string | путь к директории с `.txt` файлами |
| minWordLength | number | минимальная длина слова |
| topCount | number | количество самых частых слов |
| mode | string | режим анализа: `single` или `multi` |
| threads | number | количество потоков для многопоточного режима |
| stopWords | array | список стоп-слов |
| stopWordsFile | string | путь к файлу стоп-слов |

Пример ответа:

```json
{
  "id": 1,
  "status": "PENDING"
}
```

---

## Получение результата анализа

```
GET /api/results/{id}
```

Пример запроса:

```bash
curl -u user:user http://localhost:8080/api/results/1
```

Если анализ ещё выполняется:

```json
{
  "id": 1,
  "status": "RUNNING"
}
```

Если анализ завершён:

```json
{
  "id": 1,
  "status": "COMPLETED",
  "words": [
    {
      "word": "spring",
      "count": 15
    },
    {
      "word": "boot",
      "count": 11
    },
    {
      "word": "java",
      "count": 8
    }
  ]
}
```

---

## Получение списка анализов

```
GET /api/results
```

Пример запроса:

```bash
curl -u user:user http://localhost:8080/api/results
```

---

# Проверка авторизации

Без авторизации:

```bash
curl http://localhost:8080/api/results
```

Ответ:

```text
401 Unauthorized
```

С авторизацией:

```bash
curl -u user:user http://localhost:8080/api/results
```

---

# Аудит

Каждый запуск анализа сохраняется в таблицу:

```
audit_logs
```

В аудит записываются:

- пользователь;
- действие;
- параметры анализа;
- дата и время запуска.

Эта информация позволяет отслеживать историю использования сервиса.

---

# Хранение данных

Результаты анализа сохраняются в PostgreSQL.

Основные таблицы:

- analyses;
- analysis_words;
- analysis_errors;
- audit_logs.

Структура базы данных создаётся миграциями Flyway.

Для ускорения поиска и выборок используются индексы, которые также создаются миграциями.

---

# Особенности реализации

- REST-слой отвечает только за HTTP API.
- Сервисный слой содержит бизнес-логику.
- Операции сохранения выполняются внутри транзакций (`@Transactional`), что обеспечивает согласованность данных.
- Логика анализа текста полностью переиспользована из предыдущих домашних заданий.
- Многопоточная обработка вынесена в отдельный слой `execution`.
- Ошибка обработки одного файла не прерывает анализ остальных файлов.
- При незавершённом анализе API возвращает текущий статус выполнения.
- Для управления структурой базы данных используются миграции Flyway.

---

# CLI

CLI-режим отключён по умолчанию:

```properties
text-analyzer.cli.enabled=false
```

Для запуска CLI необходимо передать параметр:

```bash
java -jar target/text-analyzer-1.0.0.jar \
--text-analyzer.cli.enabled=true \
--dir ./texts \
--min-length 5 \
--top 10 \
--mode multi \
--threads 4
```

CLI использует ту же бизнес-логику анализа, что и REST API.

---

# Настройка окружения

Перед первым запуском создайте файл `.env` на основе шаблона:

```bash
cp .env.example .env
```

При необходимости измените значения переменных окружения.

Пример структуры `.env`:

```env
POSTGRES_DB=text_analyzer
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5433

APP_PORT=8080
SERVER_PORT=8080

DB_URL=jdbc:postgresql://postgres:5432/text_analyzer
DB_USERNAME=postgres
DB_PASSWORD=postgres

TEXT_ANALYZER_SECURITY_USER_USERNAME=user
TEXT_ANALYZER_SECURITY_USER_PASSWORD=password

TEXT_ANALYZER_SECURITY_ADMIN_USERNAME=admin
TEXT_ANALYZER_SECURITY_ADMIN_PASSWORD=admin
```
---

# Тестирование

Для запуска всех тестов выполните:

```bash
mvn test
```

Проект содержит:

- unit-тесты сервисного слоя;
- тесты REST-контроллеров;
- тесты репозиториев;
- тесты безопасности.

Перед запуском интеграционных тестов убедитесь, что PostgreSQL доступен.

---

