package com.vaultflow.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.vaultflow.driver.DriverFactory;
import com.vaultflow.model.TestResultRecord;
import com.vaultflow.model.VaultFlowTestCase;
import com.vaultflow.utils.ExcelUtils;
import com.vaultflow.utils.ExecutionSummaryWriter;
import com.vaultflow.utils.ReportManager;
import com.vaultflow.utils.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestListener implements ITestListener {
    private static final Logger LOGGER = LogManager.getLogger(TestListener.class);
    private static final List<TestResultRecord> RESULTS = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, Long> START_TIMES = new ConcurrentHashMap<>();

    @Override
    public void onStart(ITestContext context) {
        ReportManager.getReport();
        LOGGER.info("Starting VaultFlow Selenium suite: {}", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        VaultFlowTestCase testCase = getTestCase(result);
        START_TIMES.put(resultKey(result), System.currentTimeMillis());
        ExtentTest extentTest = ReportManager.getReport()
                .createTest(testCase.id() + " - " + testCase.title())
                .assignCategory(testCase.module(), testCase.scenarioType(), testCase.coverage())
                .assignAuthor("VaultFlow QA");
        ReportManager.setTest(extentTest);
        LOGGER.info("Started {} {}", testCase.id(), testCase.title());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        VaultFlowTestCase testCase = getTestCase(result);
        ReportManager.getTest().log(Status.PASS, testCase.expectedResult());
        addResult(result, "PASS", "");
        LOGGER.info("Passed {}", testCase.id());
        ReportManager.unload();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        VaultFlowTestCase testCase = getTestCase(result);
        String screenshot = ScreenshotUtils.capture(DriverFactory.getDriver(), testCase.id());
        ReportManager.getTest().log(Status.FAIL, result.getThrowable());
        if (!screenshot.isBlank()) {
            try {
                ReportManager.getTest().addScreenCaptureFromPath(screenshot);
            } catch (Exception exception) {
                LOGGER.warn("Unable to attach screenshot {}", screenshot, exception);
            }
        }
        addResult(result, "FAIL", screenshot);
        LOGGER.error("Failed {}", testCase.id(), result.getThrowable());
        ReportManager.unload();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        VaultFlowTestCase testCase = getTestCase(result);
        ReportManager.getTest().log(Status.SKIP, "Skipped: " + testCase.expectedResult());
        addResult(result, "SKIP", "");
        LOGGER.warn("Skipped {}", testCase.id());
        ReportManager.unload();
    }

    @Override
    public void onFinish(ITestContext context) {
        ReportManager.flush();
        ExcelUtils.writeExecutionSummary(RESULTS);
        ExecutionSummaryWriter.writePdfSummary(RESULTS);
        ExecutionSummaryWriter.writeLogSummary(RESULTS);
        ExecutionSummaryWriter.writeConsoleSummary(RESULTS);
        LOGGER.info("Finished VaultFlow Selenium suite: {}", context.getName());
    }

    private void addResult(ITestResult result, String status, String screenshot) {
        VaultFlowTestCase testCase = getTestCase(result);
        long startedAt = START_TIMES.getOrDefault(resultKey(result), result.getStartMillis());
        long duration = Math.max(0, System.currentTimeMillis() - startedAt);
        RESULTS.add(new TestResultRecord(
                testCase.id(),
                testCase.module(),
                testCase.title(),
                status,
                duration,
                screenshot
        ));
    }

    private VaultFlowTestCase getTestCase(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters.length > 0 && parameters[0] instanceof VaultFlowTestCase testCase) {
            return testCase;
        }
        return new VaultFlowTestCase("VF-WEB-UNKNOWN", "Unknown", result.getName(), "Unknown", "Medium", "Unknown", "");
    }

    private String resultKey(ITestResult result) {
        return result.getName() + "-" + getTestCase(result).id() + "-" + Thread.currentThread().getId();
    }
}
