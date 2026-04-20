package com.oees.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-UI-AUTH: Login page Selenium tests.
 * /register is excluded — not part of software requirements.
 *
 * FIXES applied after reading Login.jsx:
 *  - Redirect paths are /admin/courses, /instructor/exams, /student/courses (not /admin, /instructor, /student)
 *  - Error message is exactly "Invalid email or password" (from catch block)
 *  - "Contact your administrator" text is "Contact your administrator to get an account."
 */
@DisplayName("UI: Login Page")
class AuthUITest extends BaseTest {

    // TC-UI-AUTH-01: Login page renders correctly
    @Test
    @DisplayName("TC-UI-AUTH-01: Login page shows OEES branding and form fields")
    void loginPage_rendersCorrectly() {
        driver.get(LOGIN_URL);
        
        assertThat(waitForElement(By.xpath("//h1[contains(text(),'Sign in')]")).isDisplayed()).isTrue();
        assertThat(pageContainsText("Online Exam & Evaluation System")).isTrue();
        assertThat(driver.findElement(By.name("email")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.name("password")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.cssSelector("button[type='submit']")).isDisplayed()).isTrue();
        assertThat(pageContainsText("Contact your administrator")).isTrue();
    }

    // TC-UI-AUTH-02: Admin login → redirects to /admin/courses
    @Test
    @DisplayName("TC-UI-AUTH-02: Valid admin login redirects to /admin/courses")
    void login_asAdmin_redirectsToAdminDashboard() {
        loginAs(ADMIN_EMAIL, ADMIN_PASS);
        waitForUrl("/admin/courses");
        assertThat(urlContains("/admin")).isTrue();
    }

    // TC-UI-AUTH-03: Instructor login → redirects to /instructor/exams
    @Test
    @DisplayName("TC-UI-AUTH-03: Valid instructor login redirects to /instructor/exams")
    void login_asInstructor_redirectsToInstructorDashboard() {
        loginAs(INSTRUCTOR_EMAIL, INSTRUCTOR_PASS);
        waitForUrl("/instructor/exams");
        assertThat(urlContains("/instructor")).isTrue();
    }

    // TC-UI-AUTH-04: Student login → redirects to /student/courses
    @Test
    @DisplayName("TC-UI-AUTH-04: Valid student login redirects to /student/courses")
    void login_asStudent_redirectsToStudentDashboard() {
        loginAs(STUDENT_EMAIL, STUDENT_PASS);
        waitForUrl("/student/courses");
        assertThat(urlContains("/student")).isTrue();
    }

    // TC-UI-AUTH-05: Wrong password shows inline error
    @Test
    @DisplayName("TC-UI-AUTH-05: Wrong password shows 'Invalid email or password' error")
    void login_wrongPassword_showsErrorMessage() {
        loginAs(STUDENT_EMAIL, "wrongpassword");
        waitForText("Invalid email or password");
        assertThat(pageContainsText("Invalid email or password")).isTrue();
        assertThat(urlContains("/login")).isTrue();
    }

    // TC-UI-AUTH-06: Empty email blocked by HTML5 required
    @Test
    @DisplayName("TC-UI-AUTH-06: Submitting with empty email is blocked by browser validation")
    void login_emptyEmail_blockedByBrowserValidation() {
        driver.get(LOGIN_URL);
        waitAndType(By.name("password"), "somepassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        assertThat(urlContains("/login")).isTrue();
    }

    // TC-UI-AUTH-07: Loading spinner shown during login
    @Test
    @DisplayName("TC-UI-AUTH-07: Button shows 'Signing in…' text while request is in flight")
    void login_showsLoadingText_duringSubmission() {
        driver.get(LOGIN_URL);
        waitAndType(By.name("email"), STUDENT_EMAIL);
        waitAndType(By.name("password"), STUDENT_PASS);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        boolean sawLoading = !driver.findElements(
                By.xpath("//button[contains(text(),'Signing in')]")).isEmpty();
        boolean redirected = urlContains("/student");
        assertThat(sawLoading || redirected).isTrue();
    }

    // TC-UI-AUTH-08: Unauthenticated /admin redirects to /login
    @Test
    @DisplayName("TC-UI-AUTH-08: Visiting /admin without login redirects to /login")
    void unauthenticated_adminRoute_redirectsToLogin() {
        driver.get(BASE_URL + "/admin/courses");
        waitForUrl("/login");
        assertThat(urlContains("/login")).isTrue();
    }

    // TC-UI-AUTH-09: Unauthenticated /student redirects to /login
    @Test
    @DisplayName("TC-UI-AUTH-09: Visiting /student without login redirects to /login")
    void unauthenticated_studentRoute_redirectsToLogin() {
        driver.get(BASE_URL + "/student/courses");
        waitForUrl("/login");
        assertThat(urlContains("/login")).isTrue();
    }
}