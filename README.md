# QA Avito

## Задание 1: Баги на скриншоте

Анализ скриншота страницы поиска Avito — найдены и приоритизированы баги: [TASK1.md](TASK1.md)

---

## Задание 2: API-тесты

Автоматические тесты для микросервиса объявлений `https://qa-internship.avito.com`.

### Требования

Нужен **один** из вариантов:

| Вариант | Что нужно |
|---------|-----------|
| Локально | Java 17+ |
| Docker | Docker Desktop |

Maven устанавливать не нужно — есть встроенный wrapper (`mvnw`).

### Структура проекта

```
qa-avito/
├── src/test/java/com/avito/qa/
│   ├── helpers/
│   │   ├── ApiClient.java            # HTTP-клиент (RestAssured + Allure-фильтр)
│   │   └── TestDataGenerator.java    # Генерация тестовых данных
│   ├── CreateItemTest.java           # POST /api/1/item
│   ├── GetItemTest.java              # GET /api/1/item/:id
│   ├── GetSellerItemsTest.java       # GET /api/1/:sellerID/item
│   ├── GetStatisticTest.java         # GET /api/1/statistic/:id, /api/2/statistic/:id
│   ├── DeleteItemTest.java           # DELETE /api/2/item/:id
│   ├── E2eTest.java                  # E2E-сценарии с @Step
│   └── NonfunctionalTest.java        # Нефункциональные проверки
├── pom.xml                           # Maven: зависимости, Allure, Checkstyle, fmt
├── checkstyle.xml                    # Правила линтера Checkstyle
├── Dockerfile                        # Запуск тестов без Java/Maven
├── allure-screenshots/               # Скриншоты сгенерированного Allure-отчёта
├── TESTCASES.md                      # Описание тест-кейсов
├── BUGS.md                           # Найденные дефекты
├── postman_collection.json           # Postman-коллекция
└── README.md
```

### Стек технологий

- **JUnit 5** — тестовый фреймворк
- **RestAssured** — HTTP-клиент для API-тестов
- **Allure** — отчёты с описаниями, severity, шагами, логами запросов/ответов
- **Checkstyle** — линтер для Java-кода
- **google-java-format** (fmt-maven-plugin) — форматтер
- **Maven** — сборка и управление зависимостями

### Запуск тестов

### Вариант 1: Docker (Java и Maven не нужны)

Достаточно установленного [Docker Desktop](https://www.docker.com/products/docker-desktop/).

```bash
docker build -t qa-avito .
docker run --rm qa-avito
```

---

### Вариант 2: Maven Wrapper (Maven не нужен, но нужен Java 17+)

В репозитории есть `mvnw` — обёртка, которая скачает Maven автоматически.

**Windows (PowerShell / CMD):**
```bash
.\mvnw clean test
```

**Linux / macOS:**
```bash
./mvnw clean test
```

### Вариант 3: если Maven установлен

```bash
mvn clean test
```

### Запуск отдельного тест-класса

```bash
# через wrapper
.\mvnw test -Dtest=CreateItemTest

# или через Maven
mvn test -Dtest=E2eTest
```

### Allure-отчёты

### Запуск и открытие в браузере

```bash
# Запустить тесты + открыть отчёт сразу
.\mvnw clean test && .\mvnw allure:serve

# Или только сгенерировать файл
.\mvnw allure:report
```

Отчёт сохраняется в `target/site/allure-maven-plugin/index.html`.

Скриншоты сгенерированного отчёта: [`allure-screenshots/`](allure-screenshots/)

### Что отображается в отчёте

- **Название, описание и severity** каждого теста (`@DisplayName`, `@Description`, `@Severity`)
- **Шаги выполнения** — через `@Step` на методах `ApiClient` и в E2E-тестах
- **Тело запроса и ответа** — автоматически через фильтр `AllureRestAssured` в каждом запросе

### Линтер и форматтер

### Checkstyle (линтер)

Конфигурация: [`checkstyle.xml`](checkstyle.xml)

Правила:
- запрет неиспользуемых и дублирующих импортов
- методы не длиннее 60 строк
- обязательные фигурные скобки для `if`/`else`/`for`
- запрет сравнения строк через `==`
- запрет пустых `catch`-блоков
- запрет символов табуляции

```bash
# Запустить проверку
.\mvnw checkstyle:check

# Посмотреть подробный отчёт
.\mvnw checkstyle:checkstyle
# Отчёт: target/site/checkstyle.html
```

### google-java-format (форматтер)

Используется `fmt-maven-plugin` со стилем Google Java Style Guide.

```bash
# Отформатировать все файлы
.\mvnw fmt:format

# Только проверить без изменений
.\mvnw fmt:check
```

### Обнаруженные дефекты API

Описаны в [BUGS.md](BUGS.md). Основные:

| # | Описание | Серьёзность |
|---|----------|-------------|
| BUG-1 | `price: 0` отклоняется (трактуется как отсутствующее поле) | Medium |
| BUG-2 | `statistics` с нулевыми значениями отклоняется | Medium |
| BUG-3 | Отрицательная цена принимается без валидации | High |
| BUG-4 | Отрицательный `sellerID` принимается без валидации | Medium |
| BUG-5 | `/api/2/statistic/invalid` → 404 вместо 400 | Low |
| BUG-6 | Формат ответа POST не соответствует Postman-спецификации | Medium |

### Тест-кейсы

Полное описание: [TESTCASES.md](TESTCASES.md)

- **48 автоматизированных тестов**
- Позитивные, негативные, корнер-кейсы
- E2E-сценарии (полный жизненный цикл объявления)
- Нефункциональные проверки (время ответа, Content-Type)
