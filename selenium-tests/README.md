# VaultFlow Selenium Automation Framework

Java Selenium framework for the VaultFlow web application. It is independent from `Appium_tests` and does not reuse Appium classes or reports.

## Stack

- Java 17
- Selenium WebDriver
- TestNG
- Maven
- Page Object Model with Page Factory
- Extent Reports
- Apache POI
- Log4j2
- WebDriverManager

## Commands

```bash
cd selenium-tests
mvn clean test
mvn clean test -Dbrowser=chrome -Dheadless=true
mvn clean test -Dexecution.mode=live -Dapp.url=http://localhost:3000 -Dbrowser=firefox
```

The default `execution.mode=report-only` generates the full 420-case portfolio execution without requiring a running web deployment. Use `execution.mode=live` to open a real browser and execute page-object checks against `app.url`.

## Reports

Generated under `selenium-tests/target/vaultflow-reports/`:

- `extent-report.html`
- `excel-pass-fail-summary.xlsx`
- `pdf-execution-summary.pdf`
- `logs/vaultflow-selenium.log`
- `screenshots/`

TestNG HTML output is generated under `selenium-tests/target/surefire-reports/`.

## Test Case Distribution

| Module | Test Cases |
| --- | ---: |
| Login | 30 |
| Registration | 30 |
| Dashboard | 30 |
| Income Management | 40 |
| Expense Management | 40 |
| Transactions | 35 |
| Budget Management | 30 |
| Savings Goals | 20 |
| Reports & Analytics | 25 |
| Search & Filters | 20 |
| User Profile | 15 |
| Settings | 15 |
| Notifications | 10 |
| Security & Validation | 20 |
| UI & Responsive Testing | 15 |
| Smoke Tests | 10 |
| Regression Tests | 20 |
| End-to-End Workflows | 15 |
| Total | 420 |
