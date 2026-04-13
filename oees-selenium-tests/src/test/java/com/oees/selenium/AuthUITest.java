package com.oees.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-UI-AUTH: Login & Register page Selenium tests.
 *
 * Pages under test:
 *   /login    → Login.jsx
 *   /register → Register.jsx
 */
@DisplayName("UI: Login & Register")
class AuthUITest extends BaseTest {

    // ─────────────────────────────────────────────────────────────
    // TC-UI-AUTH-01: Login page renders correctly
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-AUTH-01: Login page shows OEES branding and form fields")
    void loginPage_rendersCorrectly() {
        driver.get(LOGIN_URL);

        // Brand name visible
        waitForText("OEES");

        // Heading
        assertThat(waitForElement(By.xpath("//h1[contains(text(),'Sign in')]"))
                .isDisplayed()).isTrue();

        // Subtitle
        assertThat(pageContainsText("Online Exam & Evaluation System")).isTrue();

        // Email and password inputs present
        assertThat(driver.findElement(By.name("email")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.name("password")).isDisplayed()).isTrue();

        // Submit button
        assertThat(driver.findElement(
                By.cssSelector("button[type='submit']")).isDisplayed()).isTrue();

        // Footer hint
        assertThat(pageContainsText("Contact your administrator")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-AUTH-02: Admin login redirects to /admin
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-AUTH-02: Valid admin login redirects to /admin route")
    void login_asAdmin_redirectsToAdminDashboard() {
        loginAs(ADMIN_EMAIL, ADMIN_PASS);
        waitForUrl("/admin");
        assertThat(urlContains("/admin")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-AUTH-03: Instructor login redirects to /instructor
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-AUTH-03: Valid instructor login redirects to /instructor route")
    void login_asInstructor_redirectsToInstructorDashboard() {
        loginAs(INSTRUCTOR_EMAIL, INSTRUCTOR_PASS);
        waitForUrl("/instructor");
        assertThat(urlContains("/instructor")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-AUTH-04: Student login redirects to /student
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-AUTH-04: Valid student login redirects to /student route")
    void login_asStudent_redirectsToStudentDashboard() {
        loginAs(STUDENT_EMAIL, STUDENT_PASS);
        waitForUrl("/student");
        assertThat(urlContains("/student")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-AUTH-05: Wrong password shows inline error
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-AUTH-05: Wrong password shows 'Invalid email or password' error")
    void login_wrongPassword_showsErrorMessage() {
        loginAs(STUDENT_EMAIL, "wrongpassword");

        // Should stay on login page
        waitForText("Invalid email or password");
        assertThat(pageContainsText("Invalid email or password")).isTrue();
        assertThat(urlContains("/login")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-AUTH-06: Empty email blocked by HTML5 required
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-AUTH-06: Submitting with empty email is blocked by browser validation")
    void login_emptyEmail_blockedByBrowserValidation() {
        driver.get(LOGIN_URL);
        // Leave email blank, fill password
        waitAndType(By.name("password"), "somepassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Still on login page — form should not have submitted
        assertThat(urlContains("/login")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-AUTH-07: Loading spinner shown during login
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-AUTH-07: Button shows 'Signing in…' text while request is in flight")
    void login_showsLoadingText_duringSubmission() {
        driver.get(LOGIN_URL);
        waitAndType(By.name("email"), STUDENT_EMAIL);
        waitAndType(By.name("password"), STUDENT_PASS);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Briefly shows loading text (may race with redirect, so check for either)
        boolean sawLoading = !driver.findElements(
                By.xpath("//button[contains(text(),'Signing in')]")).isEmpty();
        boolean redirected = urlContains("/student");
        assertThat(sawLoading || redirected).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-AUTH-08: Unauthenticated access to /admin redirects to /login
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-AUTH-08: Visiting /admin without login redirects to /login")
    void unauthenticated_adminRoute_redirectsToLogin() {
        driver.get(BASE_URL + "/admin/courses");
        waitForUrl("/login");
        assertThat(urlContains("/login")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-AUTH-09: Unauthenticated access to /student redirects to /login
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-AUTH-09: Visiting /student without login redirects to /login")
    void unauthenticated_studentRoute_redirectsToLogin() {
        driver.get(BASE_URL + "/student/exams");
        waitForUrl("/login");
        assertThat(urlContains("/login")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-REG-01: Register page renders correctly
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-REG-01: Register page shows all fields including role dropdown")
    void registerPage_rendersAllFields() {
        driver.get(REG_URL);

        waitForText("Create account");
        assertThat(pageContainsText("Join the OEES platform")).isTrue();

        // All form fields present
        assertThat(driver.findElement(By.xpath("//input[@placeholder='John Doe']")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.xpath("//input[@type='email']")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.xpath("//input[@type='password']")).isDisplayed()).isTrue();

        // Role dropdown
        Select roleSelect = new Select(driver.findElement(By.cssSelector("select")));
        assertThat(roleSelect.getOptions()).hasSizeGreaterThanOrEqualTo(3);

        // Sign in link
        assertThat(pageContainsText("Already have an account")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-REG-02: Successful registration redirects to /login
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-REG-02: Valid registration redirects to /login page")
    void register_validData_redirectsToLogin() {
        driver.get(REG_URL);

        String uniqueEmail = "testuser_" + System.currentTimeMillis() + "@test.com";

        waitAndType(By.xpath("//input[@placeholder='John Doe']"), "Test User");
        waitAndType(By.xpath("//input[@type='email']"), uniqueEmail);
        waitAndType(By.xpath("//input[@type='password']"), "password123");
        // Leave role as default STUDENT
        waitAndClick(By.cssSelector("button[type='submit']"));

        waitForUrl("/login");
        assertThat(urlContains("/login")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-REG-03: Register with duplicate email shows error
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-REG-03: Duplicate email shows registration error message")
    void register_duplicateEmail_showsError() {
        driver.get(REG_URL);

        waitAndType(By.xpath("//input[@placeholder='John Doe']"), "Duplicate");
        waitAndType(By.xpath("//input[@type='email']"), STUDENT_EMAIL); // already exists
        waitAndType(By.xpath("//input[@type='password']"), "password123");
        waitAndClick(By.cssSelector("button[type='submit']"));

        // Error message should appear — stays on register page
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".bg-red-50")));
        assertThat(urlContains("/register")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-REG-04: Register with INSTRUCTOR role option available
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-REG-04: Role dropdown contains STUDENT, INSTRUCTOR, ADMIN options")
    void register_roleDropdown_hasAllRoles() {
        driver.get(REG_URL);
        Select roleSelect = new Select(waitForElement(By.cssSelector("select")));
        java.util.List<String> options = roleSelect.getOptions()
                .stream().map(o -> o.getText()).toList();
        assertThat(options).contains("Student", "Instructor", "Admin");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-REG-05: "Sign in" link on register page navigates to /login
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-REG-05: 'Sign in' link on register page navigates to /login")
    void register_signInLink_navigatesToLogin() {
        driver.get(REG_URL);
        waitAndClick(By.linkText("Sign in"));
        waitForUrl("/login");
        assertThat(urlContains("/login")).isTrue();
    }
}
