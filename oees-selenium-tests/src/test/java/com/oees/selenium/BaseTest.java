package com.oees.selenium;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

public abstract class BaseTest {
    protected static final String BASE_URL = "http://localhost:5173";
    protected static final String LOGIN_URL = "http://localhost:5173/login";
    protected static final String REG_URL = "http://localhost:5173/register";
    protected static final String ADMIN_EMAIL = "admin@oees.com";
    protected static final String ADMIN_PASS = "admin123";
    protected static final String INSTRUCTOR_EMAIL = "instructor@oees.com";
    protected static final String INSTRUCTOR_PASS = "instructor123";
    protected static final String STUDENT_EMAIL = "student@oees.com";
    protected static final String STUDENT_PASS = "student123";
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BaseTest() {
    }

    @BeforeEach
    void setUpDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1400,900");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(LOGIN_URL);
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void loginAs(String email, String password) {
        driver.get(LOGIN_URL);
        waitAndType(By.name("email"), email);
        waitAndType(By.name("password"), password);
        waitAndClick(By.cssSelector("button[type='submit']"));
    }

    protected void waitAndType(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    protected void waitAndClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
    }

    protected void waitForUrl(String substring) {
        wait.until(ExpectedConditions.urlContains(substring));
    }

    protected void waitForText(String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(),'" + text + "')]")));
    }

    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void selectOption(By locator, String visibleText) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        new Select(element).selectByVisibleText(visibleText);
    }

    protected boolean urlContains(String substring) {
        return driver.getCurrentUrl().contains(substring);
    }

    protected boolean pageContainsText(String text) {
        return !driver.findElements(By.xpath("//*[contains(text(),'" + text + "')]")).isEmpty();
    }
}