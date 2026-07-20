package com.vaultflow.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class ElementActions {
    private final WebDriver driver;
    private final WaitUtils wait;

    public ElementActions(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    public void click(By locator) {
        wait.clickable(locator).click();
    }

    public void type(By locator, String value) {
        wait.visible(locator).clear();
        wait.visible(locator).sendKeys(value);
    }

    public void pressEnter(By locator) {
        wait.visible(locator).sendKeys(Keys.ENTER);
    }

    public void hover(By locator) {
        new Actions(driver).moveToElement(wait.visible(locator)).perform();
    }

    public void keyboardShortcut(Keys modifier, String key) {
        new Actions(driver).keyDown(modifier).sendKeys(key).keyUp(modifier).perform();
    }
}
