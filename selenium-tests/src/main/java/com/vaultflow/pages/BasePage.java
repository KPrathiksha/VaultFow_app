package com.vaultflow.pages;

import com.vaultflow.utils.ElementActions;
import com.vaultflow.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WaitUtils wait;
    protected final ElementActions actions;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.actions = new ElementActions(driver);
        PageFactory.initElements(driver, this);
    }

    protected By testId(String value) {
        return By.cssSelector("[data-testid='" + value + "']");
    }

    protected boolean isDisplayed(By locator) {
        try {
            return wait.visible(locator).isDisplayed();
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
