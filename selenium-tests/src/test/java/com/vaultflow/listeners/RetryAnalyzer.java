package com.vaultflow.listeners;

import com.vaultflow.config.ConfigReader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int attempts;
    private final int maxAttempts = ConfigReader.getInt("retry.count", 1);

    @Override
    public boolean retry(ITestResult result) {
        if (attempts < maxAttempts) {
            attempts++;
            return true;
        }
        return false;
    }
}
