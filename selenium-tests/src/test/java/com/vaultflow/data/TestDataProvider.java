package com.vaultflow.data;

import org.testng.annotations.DataProvider;

public final class TestDataProvider {
    private TestDataProvider() {
    }

    @DataProvider(name = "vaultFlowCases", parallel = true)
    public static Object[][] vaultFlowCases() {
        return TestCatalog.build().stream()
                .map(testCase -> new Object[]{testCase})
                .toArray(Object[][]::new);
    }
}
