package com.vaultflow.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RegistrationPage extends BasePage {
    @FindBy(css = "[data-testid='register-name'], input[name='name']")
    private WebElement nameInput;

    @FindBy(css = "[data-testid='register-email'], input[type='email'], input[name='email']")
    private WebElement emailInput;

    @FindBy(css = "[data-testid='register-password'], input[type='password'], input[name='password']")
    private WebElement passwordInput;

    @FindBy(css = "[data-testid='register-submit'], button[type='submit']")
    private WebElement submitButton;

    public RegistrationPage(WebDriver driver) {
        super(driver);
    }

    public void register(String name, String email, String password) {
        nameInput.clear();
        nameInput.sendKeys(name);
        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        submitButton.click();
    }
}
