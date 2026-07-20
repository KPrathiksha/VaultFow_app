package com.vaultflow.tests;

import com.vaultflow.config.ConfigReader;
import com.vaultflow.constants.FrameworkConstants;
import com.vaultflow.driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.nio.file.Files;

public abstract class BaseTest {
    @BeforeSuite(alwaysRun = true)
    public void prepareReportFolders() throws IOException {
        Files.createDirectories(FrameworkConstants.REPORT_DIR);
        Files.createDirectories(FrameworkConstants.SCREENSHOT_DIR);
        Files.createDirectories(FrameworkConstants.LOG_DIR);
    }

    @BeforeMethod(alwaysRun = true)
    public void openApplicationWhenLive() {
        if (ConfigReader.isLiveExecution()) {
            WebDriver driver = DriverFactory.createDriver();
            driver.get(ConfigReader.get("app.url", "http://localhost:3000"));
        }
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowserWhenLive() {
        if (ConfigReader.isLiveExecution()) {
            DriverFactory.quitDriver();
        }
    }
}
