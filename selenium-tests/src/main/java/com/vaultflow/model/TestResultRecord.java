package com.vaultflow.model;

public record TestResultRecord(
        String id,
        String module,
        String title,
        String status,
        long durationMillis,
        String screenshotPath
) {
}
