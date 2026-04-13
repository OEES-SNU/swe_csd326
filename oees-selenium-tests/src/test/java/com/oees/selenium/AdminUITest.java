package com.oees.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-UI-ADMIN: Admin pages Selenium tests.
 *
 * Pages under test:
 *   /admin/courses     → Courses.jsx
 *   /admin/students    → Students.jsx
 *   /admin/enrollments → Enrollments.jsx
 */
@DisplayName("UI: Admin Pages")
class AdminUITest extends BaseTest {

    @BeforeEach
    void loginAsAdmin() {
        loginAs(ADMIN_EMAIL, ADMIN_PASS);
        waitForUrl("/admin");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-ADMIN-01: Admin dashboard loads with sidebar
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-ADMIN-01: Admin dashboard renders sidebar navigation")
    void adminDashboard_rendersSidebar() {
        // Sidebar should have navigation items for Courses, Students, Enrollments
        waitForText("Courses");
        assertThat(pageContainsText("Students")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-ADMIN-02: Courses page loads and shows table
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-ADMIN-02: Courses page loads with table headers and 'Add course' button")
    void coursesPage_rendersTableAndButton() {
        driver.get(BASE_URL + "/admin/courses");

        waitForText("Courses");
        assertThat(waitForElement(By.xpath("//h1[contains(text(),'Courses')]")).isDisplayed()).isTrue();

        // Table headers
        assertThat(pageContainsText("Code")).isTrue();
        assertThat(pageContainsText("Name")).isTrue();
        assertThat(pageContainsText("Instructor")).isTrue();
        assertThat(pageContainsText("Status")).isTrue();

        // Add course button
        assertThat(waitForElement(
                By.xpath("//button[contains(text(),'Add course')]")).isDisplayed()).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-ADMIN-03: Open and cancel Create Course modal
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-ADMIN-03: 'Add course' opens modal; 'Cancel' closes it")
    void coursesPage_addCourseModal_openAndClose() {
        driver.get(BASE_URL + "/admin/courses");

        waitAndClick(By.xpath("//button[contains(text(),'Add course')]"));

        // Modal title visible
        waitForText("Create Course");
        assertThat(pageContainsText("Course code")).isTrue();
        assertThat(pageContainsText("Course name")).isTrue();

        // Cancel closes modal
        waitAndClick(By.xpath("//button[contains(text(),'Cancel')]"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//h2[contains(text(),'Create Course')]")));
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-ADMIN-04: Create a new course successfully
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-ADMIN-04: Creating a course with valid data adds it to the table")
    void coursesPage_createCourse_appearsInTable() {
        driver.get(BASE_URL + "/admin/courses");

        waitAndClick(By.xpath("//button[contains(text(),'Add course')]"));
        waitForText("Create Course");

        String code = "TEST" + System.currentTimeMillis() % 10000;
        waitAndType(By.xpath("//input[@placeholder='e.g. CS101']"), code);
        waitAndType(By.xpath("//input[@placeholder='e.g. Introduction to Computing']"), "Test Course");

        waitAndClick(By.xpath("//button[contains(text(),'Create') and @type='submit']"));

        // Modal closes and new row appears
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//h2[contains(text(),'Create Course')]")));
        waitForText(code);
        assertThat(pageContainsText(code)).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-ADMIN-05: Create course with empty code shows validation
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-ADMIN-05: Creating course with empty code is blocked by required validation")
    void coursesPage_createCourse_emptyCode_blocked() {
        driver.get(BASE_URL + "/admin/courses");
        waitAndClick(By.xpath("//button[contains(text(),'Add course')]"));
        waitForText("Create Course");

        // Leave code blank, fill name
        waitAndType(By.xpath("//input[@placeholder='e.g. Introduction to Computing']"), "Test");
        waitAndClick(By.xpath("//button[contains(text(),'Create') and @type='submit']"));

        // Modal stays open (required field not filled)
        assertThat(pageContainsText("Create Course")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-ADMIN-06: Assign instructor modal opens from course row
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-ADMIN-06: 'Assign instructor' button opens assignment modal")
    void coursesPage_assignInstructorModal_opens() {
        driver.get(BASE_URL + "/admin/courses");

        // Wait for table to load
        waitForText("Courses");

        // Click first "Assign instructor" button
        waitAndClick(By.xpath("(//button[contains(text(),'Assign instructor')])[1]"));

        // Modal opens with instructor dropdown
        waitForText("Assign Instructor");
        assertThat(pageContainsText("Instructor")).isTrue();

        // Cancel it
        waitAndClick(By.xpath("//button[contains(text(),'Cancel')]"));
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-ADMIN-07: Students page loads and shows student list
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-ADMIN-07: Students page loads with table headers")
    void studentsPage_rendersTable() {
        driver.get(BASE_URL + "/admin/students");

        waitForText("Students");
        assertThat(pageContainsText("Name")).isTrue();
        assertThat(pageContainsText("Email")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-ADMIN-08: Enrollments page loads
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-ADMIN-08: Enrollments page loads correctly")
    void enrollmentsPage_renders() {
        driver.get(BASE_URL + "/admin/enrollments");
        waitForText("Enroll");
        assertThat(urlContains("/admin")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-ADMIN-09: Student role cannot access /admin (role guard)
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-ADMIN-09: After admin logout, /admin is inaccessible")
    void adminRoute_afterLogout_isInaccessible() {
        // Navigate to login (simulating a logout / new session)
        driver.get(LOGIN_URL);

        // Try to access admin directly (without auth)
        driver.get(BASE_URL + "/admin/courses");
        waitForUrl("/login");
        assertThat(urlContains("/login")).isTrue();
    }
}
