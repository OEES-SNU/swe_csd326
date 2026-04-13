package com.oees.selenium;

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

import java.time.Duration;

/**
 * Base class for all OEES Selenium tests.
 *
 * Prerequisites before running:
 *  1. Backend running at http://localhost:8080
 *  2. Frontend (npm run dev) running at http://localhost:5173
 *  3. ChromeDriver installed and on PATH  (or set system property webdriver.chrome.driver)
 *
 * Run all tests:   mvn test   (from this selenium-tests folder)
 */
public abstract class BaseTest {

    protected static final String BASE_URL  = "http://localhost:5173";
    protected static final String LOGIN_URL = BASE_URL + "/login";
    protected static final String REG_URL   = BASE_URL + "/register";

    // Shared test accounts — make sure these exist in the DB before running
    protected static final String ADMIN_EMAIL       = "admin@test.com";
    protected static final String ADMIN_PASS        = "password123";
    protected static final String INSTRUCTOR_EMAIL  = "instructor@test.com";
    protected static final String INSTRUCTOR_PASS   = "password123";
    protected static final String STUDENT_EMAIL     = "student@test.com";
    protected static final String STUDENT_PASS      = "password123";

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    void setUpDriver() {
        ChromeOptions opts = new ChromeOptions();
        // opts.addArguments("--headless");   // uncomment to run headlessly
        opts.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1400,900");
        driver = new ChromeDriver(opts);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(LOGIN_URL);
    }

    @AfterEach
    void tearDownDriver() {
        if (driver != null) driver.quit();
    }

    // ── Helpers ──────────────────────────────────────────────────

    /** Fill and submit the login form. */
    protected void loginAs(String email, String password) {
        driver.get(LOGIN_URL);
        waitAndType(By.name("email"), email);
        waitAndType(By.name("password"), password);
        waitAndClick(By.cssSelector("button[type='submit']"));
    }

    /** Wait for an element to be visible then type into it. */
    protected void waitAndType(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    /** Wait for element to be clickable then click. */
    protected void waitAndClick(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /** Wait for the URL to contain a given substring. */
    protected void waitForUrl(String substring) {
        wait.until(ExpectedConditions.urlContains(substring));
    }

    /** Wait for visible text to appear anywhere in the page. */
    protected void waitForText(String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'" + text + "')]")));
    }

    /** Wait for an element to be present in the DOM. */
    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Select a value in a <select> element. */
    protected void selectOption(By locator, String visibleText) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        new Select(el).selectByVisibleText(visibleText);
    }

    /** Check whether the current URL contains the given string. */
    protected boolean urlContains(String s) {
        return driver.getCurrentUrl().contains(s);
    }

    /** Check if any element with the given text is currently visible. */
    protected boolean pageContainsText(String text) {
        return !driver.findElements(
                By.xpath("//*[contains(text(),'" + text + "')]")).isEmpty();
    }
}
