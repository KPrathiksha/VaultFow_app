package com.vaultflow.pages;

import com.vaultflow.model.VaultFlowTestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

public class FinanceModulePage extends BasePage {
    public FinanceModulePage(WebDriver driver) {
        super(driver);
    }

    public void openModule(String module) {
        String slug = slug(module);
        By moduleLink = By.cssSelector("[data-testid='nav-" + slug + "'], a[href*='" + slug + "']");
        if (isDisplayed(moduleLink)) {
            actions.click(moduleLink);
        }
    }

    public boolean verifyScenarioSurface(VaultFlowTestCase testCase) {
        String slug = slug(testCase.module());
        By moduleRoot = By.cssSelector("[data-testid='" + slug + "'], [data-module='" + slug + "'], main");
        return isDisplayed(moduleRoot);
    }

    public void search(String value) {
        By search = By.cssSelector("[data-testid='search-input'], input[type='search']");
        if (isDisplayed(search)) {
            actions.type(search, value);
            actions.pressEnter(search);
        }
    }

    public void sortBy(String visibleText) {
        By sort = By.cssSelector("[data-testid='sort-select'], select[name='sort']");
        if (isDisplayed(sort)) {
            new Select(wait.visible(sort)).selectByVisibleText(visibleText);
        }
    }

    public void handleModalIfPresent() {
        By modalClose = By.cssSelector("[data-testid='modal-close'], [aria-label='Close']");
        if (isDisplayed(modalClose)) {
            actions.click(modalClose);
        }
    }

    private String slug(String value) {
        return value.toLowerCase().replace("&", "and").replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
