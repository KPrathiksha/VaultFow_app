package com.vaultflow.tests;

import com.vaultflow.config.ConfigReader;
import com.vaultflow.data.TestDataProvider;
import com.vaultflow.driver.DriverFactory;
import com.vaultflow.model.VaultFlowTestCase;
import com.vaultflow.pages.DashboardPage;
import com.vaultflow.pages.FinanceModulePage;
import com.vaultflow.pages.LoginPage;
import com.vaultflow.pages.RegistrationPage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class VaultFlowPortfolioTest extends BaseTest {
    @Test(dataProvider = "vaultFlowCases", dataProviderClass = TestDataProvider.class)
    public void executeVaultFlowSeleniumScenario(VaultFlowTestCase testCase) {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertFalse(testCase.id().isBlank(), "Test case id must be present");
        softAssert.assertFalse(testCase.module().isBlank(), "Module must be present");
        softAssert.assertFalse(testCase.title().isBlank(), "Title must be present");
        softAssert.assertFalse(testCase.expectedResult().isBlank(), "Expected result must be present");

        if (ConfigReader.isLiveExecution()) {
            executeLiveScenario(testCase, softAssert);
        }

        softAssert.assertAll();
    }

    private void executeLiveScenario(VaultFlowTestCase testCase, SoftAssert softAssert) {
        WebDriver driver = DriverFactory.getDriver();
        switch (testCase.module()) {
            case "Login" -> {
                LoginPage loginPage = new LoginPage(driver);
                softAssert.assertTrue(loginPage.isLoginFormVisible(), "Login form should be visible");
                if ("Positive".equals(testCase.scenarioType())) {
                    loginPage.login(ConfigReader.get("valid.email"), ConfigReader.get("valid.password"));
                }
            }
            case "Registration" -> new RegistrationPage(driver)
                    .register("VaultFlow QA", "qa+" + testCase.id().toLowerCase() + "@example.com", "VaultFlow@123");
            case "Dashboard" -> {
                DashboardPage dashboardPage = new DashboardPage(driver);
                softAssert.assertTrue(dashboardPage.isLoaded(), "Dashboard should load");
                softAssert.assertTrue(dashboardPage.hasNavigation(), "Primary navigation should be visible");
            }
            default -> {
                FinanceModulePage modulePage = new FinanceModulePage(driver);
                modulePage.openModule(testCase.module());
                modulePage.search(testCase.module());
                modulePage.handleModalIfPresent();
                softAssert.assertTrue(modulePage.verifyScenarioSurface(testCase),
                        "Module surface should be available for " + testCase.module());
            }
        }
    }
}
