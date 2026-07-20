package com.vaultflow.constants;

import com.vaultflow.config.ConfigReader;

import java.nio.file.Path;

public final class FrameworkConstants {
    public static final String SUITE_NAME = "VaultFlow Selenium Automation Suite";
    public static final String FRAMEWORK = "Selenium + Java + TestNG";
    public static final String EXPECTED_EXECUTION_TIME = "52 min 14 sec";
    public static final Path REPORT_DIR = Path.of(ConfigReader.get("reports.dir", "target/vaultflow-reports"));
    public static final Path SCREENSHOT_DIR = REPORT_DIR.resolve("screenshots");
    public static final Path LOG_DIR = REPORT_DIR.resolve("logs");

    private FrameworkConstants() {
    }
}
