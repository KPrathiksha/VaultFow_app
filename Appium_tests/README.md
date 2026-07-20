# VaultFlow Appium Portfolio Suite

This folder contains the 400-case mobile automation portfolio suite used by the root `npm test` script. The suite models Android Appium coverage for VaultFlow and generates the reporting artifacts expected from an industry-level QA automation portfolio.

## Commands

```bash
npm test
npm run test:appium:portfolio
npm run test:appium:portfolio:report
npm run test:selenium:portfolio
npm run test:selenium:portfolio:report
npm run test:selenium:portfolio -- --report-only --fail 3
```

The default run generates:

- `selenium-tests/reports/extent-report.html`
- `selenium-tests/reports/testng-report.html`
- `selenium-tests/reports/excel-summary.xlsx`
- `selenium-tests/reports/pdf-test-execution-summary.pdf`
- `selenium-tests/reports/test-coverage-report.html`
- `selenium-tests/reports/performance-summary.html`
- `selenium-tests/reports/allure-results/`
- `selenium-tests/reports/screenshots/`
- `selenium-tests/reports/execution.log`

## Coverage Distribution

The source checklist contained 450 cases when summed literally, so this project keeps all requested modules and normalizes the heavier modules to produce exactly 400 portfolio cases.

| Module | Appium Test Cases |
| --- | ---: |
| App Launch & Splash Screen | 10 |
| Login | 25 |
| Registration | 25 |
| Forgot Password & OTP | 20 |
| Dashboard | 25 |
| Income Management | 30 |
| Expense Management | 30 |
| Transaction History | 25 |
| Budget Management | 20 |
| Savings Goals | 15 |
| Reports & Analytics | 15 |
| Notifications & Reminders | 15 |
| Search, Filter & Sorting | 20 |
| User Profile | 15 |
| Settings | 15 |
| AI Budget Suggestions | 15 |
| Security & Validation | 15 |
| Offline Functionality | 10 |
| Device Permissions | 10 |
| UI/UX & Responsive Testing | 10 |
| Navigation & Gesture Testing | 10 |
| Performance & Stability | 10 |
| Smoke & Regression Tests | 15 |
| Total | 400 |

`run-e2e.js` is reserved for live browser checks. Set `VAULTFLOW_APP_URL` when you have the web app running.

Use `--fail <count>` to intentionally mark the first cases as failed and generate linked screenshot evidence for report validation.
