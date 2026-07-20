package com.vaultflow.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {
    @FindBy(css = "[data-testid='login-email'], input[type='email'], input[name='email']")
    private WebElement emailInput;

    @FindBy(css = "[data-testid='login-password'], input[type='password'], input[name='password']")
    private WebElement passwordInput;

    @FindBy(css = "[data-testid='login-submit'], button[type='submit']")
    private WebElement submitButton;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void login(String email, String password) {
        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        submitButton.click();
    }

    public boolean isLoginFormVisible() {
        return emailInput.isDisplayed() && passwordInput.isDisplayed();
    }
}
