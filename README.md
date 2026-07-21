# playwright-java-framework-v3-main

Master Test Automation Framework — Java + Maven + Playwright + Cucumber BDD + Allure Reporting

## What's inside

| Area | Package / Location |
|------|--------------------|
| UI Framework (Playwright + Cucumber + POM) | `com.upskill.*` |
| UI evolution steps (plain main → JUnit → Cucumber) | `com.upskill.evolution.*` |
| API Testing (RestAssured + Allure) | `testcase/restapi` |
| DB Testing (JDBC + PostgreSQL + H2) | `javaPrograms/JDBC*.java` |
| Java Practice Programs | `javaPrograms/` |
| Selenium Interview Concepts | `seleniumInterviewConcepts/` |
| Playwright Advanced Features | `testcase/` |
| BDD Feature Files | `src/test/resources/features/` |

## Stack

- **Playwright 1.42** — UI automation
- **Cucumber 7.15** — BDD / Gherkin
- **JUnit 4 + JUnit 5** — Test runners
- **Allure 2.25** — Reporting
- **RestAssured 5.4** — API testing
- **PostgreSQL / H2 / JDBC** — DB testing
- **Selenium 4.22** — Interview concepts

## Run tests

```bash
# All Cucumber tests (default)
mvn test

# Smoke only
mvn test -Psmoke

# Regression
mvn test -Pregression

# Specific tags
mvn test "-Dcucumber.filter.tags=@login"

# Different browser
mvn test -Dbrowser=firefox

# Generate Allure report
mvn allure:serve
```

## Config

Edit `src/test/resources/config-qa.properties` (QA) or `config-dev.properties` (dev).

## Bene App (Spring Boot backend)

Companion backend project: `bene-app-api-boot` — run it locally before executing API/DB tests.
