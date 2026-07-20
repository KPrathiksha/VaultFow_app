package com.vaultflow.model;

public record VaultFlowTestCase(
        String id,
        String module,
        String title,
        String scenarioType,
        String priority,
        String coverage,
        String expectedResult
) {
    @Override
    public String toString() {
        return id + " - " + title;
    }
}
