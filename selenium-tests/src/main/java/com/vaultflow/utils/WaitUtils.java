package com.vaultflow.utils;

import com.vaultflow.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitUtils {
    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver) {
        this.wait = new WebDriverWait(driver, ConfigReader.getDuration("explicit.wait.seconds", 15));
    }

    public WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean urlContains(String value) {
        return wait.until(ExpectedConditions.urlContains(value));
    }

    public boolean textPresent(By locator, String value) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, value));
    }
}
