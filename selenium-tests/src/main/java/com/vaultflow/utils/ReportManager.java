package com.vaultflow.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.vaultflow.constants.FrameworkConstants;

import java.io.IOException;
import java.nio.file.Files;

public final class ReportManager {
    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    private ReportManager() {
    }

    public static synchronized ExtentReports getReport() {
        if (extentReports == null) {
            try {
                Files.createDirectories(FrameworkConstants.REPORT_DIR);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to create report directory", exception);
            }
            ExtentSparkReporter spark = new ExtentSparkReporter(
                    FrameworkConstants.REPORT_DIR.resolve("extent-report.html").toString());
            spark.config().setDocumentTitle(FrameworkConstants.SUITE_NAME);
            spark.config().setReportName(FrameworkConstants.SUITE_NAME);

            extentReports = new ExtentReports();
            extentReports.attachReporter(spark);
            extentReports.setSystemInfo("Browser", com.vaultflow.config.ConfigReader.get("browser", "chrome"));
            extentReports.setSystemInfo("Framework", FrameworkConstants.FRAMEWORK);
            extentReports.setSystemInfo("Execution Mode", com.vaultflow.config.ConfigReader.get("execution.mode", "report-only"));
        }
        return extentReports;
    }

    public static void setTest(ExtentTest test) {
        TEST.set(test);
    }

    public static ExtentTest getTest() {
        return TEST.get();
    }

    public static void unload() {
        TEST.remove();
    }

    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
        }
    }
}
