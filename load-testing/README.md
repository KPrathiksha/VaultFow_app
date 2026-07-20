# VaultFlow Load Testing

Independent k6 framework with a generated catalog of 420 scenarios. Run from this directory:

```sh
VAULTFLOW_TOKEN='token' npm run run
```

Use `VAULTFLOW_EMAIL` and `VAULTFLOW_PASSWORD` instead to authenticate once and reuse the session token. The baseline is 100 VUs, a 1-minute constant arrival rate, and 1–3 second think time. A single catalog case can run independently with `SCENARIO_ID=VF-LT-001 npm run load`.

Outputs in `reports/`: `performance-report.xlsx`, interactive HTML report with response-time graph, `summary.csv`, `scenario-results.csv`, JSON results, execution log, and error summary. The backend's present 100-requests-per-minute limiter must be relaxed only in a separate load-test deployment before running this 100 requests/sec profile.
