package com.vaultflow.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {
    private final By dashboardRoot = testId("dashboard");
    private final By balanceCard = testId("dashboard-balance");
    private final By navigation = testId("primary-navigation");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isLoaded() {
        return isDisplayed(dashboardRoot) || isDisplayed(balanceCard);
    }

    public boolean hasNavigation() {
        return isDisplayed(navigation);
    }
}
