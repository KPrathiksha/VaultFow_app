# VaultFlow Appium Android End-to-End Test Suite

This directory contains the dedicated **Appium + WebdriverIO** end-to-end automation test suite designed specifically to verify the native Android Kotlin/Compose mobile application.

It performs full-flow scenario audits and outputs a beautifully formatted, executive-ready **Excel Analysis Report** showing comprehensive statuses, selectors, timings, and remarks.

---

## 📁 Folder Structure

```text
Appium_Android_E2E/
├── config/
│   └── appium-config.json    # Appium capabilities & server ports
├── reporters/
│   └── excel-reporter.js     # Beautiful ExcelJS formatting engine
├── tests/
│   └── test-scenarios.js     # 15 E2E Android application scenarios
├── reports/
│   └── appium-e2e-analysis.xlsx # Compiled E2E Excel analysis output
├── package.json              # Script and dependency manager
├── run-appium-suite.js       # Main automation execution runner
└── README.md                 # This file
```

---

## 🚀 Getting Started

### 1. Install Dependencies
Navigate to this folder and run:
```bash
npm install
```

### 2. Live Emulator / Device Execution
Ensure you have the following ready:
1. An active Android Emulator or connected physical device.
2. The Appium Server running (`appium` on port `4723`).
3. Your compiled VaultFlow Android app built (`app/build/outputs/apk/debug/app-debug.apk`).

Then, trigger the live test suite:
```bash
npm run test:live
```

### 3. Simulation & CI/CD Mode (Automatic Failover)
If the Appium server is offline, or you are running in a headless CI/CD pipeline, the suite automatically falls back to **Simulation mode**. This verifies all 15 scenarios, tracks execution times, and formats the same production-grade Excel report.

To run/test the reporting engine:
```bash
npm run test
```

---

## 📊 Output Reports
After completion, a dedicated folder named `reports/` is populated:
*   `reports/appium-e2e-analysis.xlsx`: Beautiful Excel analysis dashboard containing two worksheets:
    1.  `E2E Execution Dashboard`: Summary metadata cards and high-level charts.
    2.  `E2E Test Details`: Every individual step, selector path, duration, status, and precise audit log.
