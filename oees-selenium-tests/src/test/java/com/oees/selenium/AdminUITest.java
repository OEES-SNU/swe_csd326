package com.oees.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-UI-ADMIN: Admin pages Selenium tests.
 *
 * FIXES applied after reading Courses.jsx:
 *  - Modal title is "Create Course" (correct) — but the button text is "Create" not "Create course"
 *  - Assign instructor modal title is "Assign Instructor — <courseName>" not just "Assign Instructor"
 *  - Admin-09: clears localStorage keys 'token' and 'user' exactly as AuthContext.jsx stores them
 */
@DisplayName("UI: Admin Pages")
class AdminUITest extends BaseTest {

    private void openCreateCourseModal() {
        org.openqa.selenium.WebElement addBtn = waitForElement(By.xpath("//button[contains(text(),'Add course')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@placeholder='e.g. CS101']")));
    }

    private void openAssignInstructorModal() {
        org.openqa.selenium.WebElement assignBtn = waitForElement(By.xpath("(//button[contains(text(),'Assign instructor')])[1]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", assignBtn);
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//select[contains(@class,'select')]")));
    }

    private WebElement getOpenCreateModal() {
        return waitForElement(By.xpath("//div[contains(@class,'fixed') and .//input[@placeholder='e.g. CS101']]"));
    }

    @BeforeEach
    void loginAsAdmin() {
        loginAs(ADMIN_EMAIL, ADMIN_PASS);
        waitForUrl("/admin/courses");
    }

    // TC-UI-ADMIN-01: Admin dashboard loads with sidebar
    @Test
    @DisplayName("TC-UI-ADMIN-01: Admin dashboard renders sidebar navigation")
    void adminDashboard_rendersSidebar() {
        waitForText("Courses");
        assertThat(waitForElement(By.xpath("//a[@href='/admin/students' and contains(normalize-space(),'Users')]")).isDisplayed()).isTrue();
    }

    // TC-UI-ADMIN-02: Courses page loads with table and Add button
    @Test
    @DisplayName("TC-UI-ADMIN-02: Courses page loads with table headers and 'Add course' button")
    void coursesPage_rendersTableAndButton() {
        driver.get(BASE_URL + "/admin/courses");
        waitForText("Courses");
        assertThat(waitForElement(By.xpath("//h1[contains(text(),'Courses')]")).isDisplayed()).isTrue();
        assertThat(pageContainsText("Code")).isTrue();
        assertThat(pageContainsText("Name")).isTrue();
        assertThat(pageContainsText("Instructor")).isTrue();
        assertThat(pageContainsText("Status")).isTrue();
        assertThat(waitForElement(
                By.xpath("//button[contains(text(),'Add course')]")).isDisplayed()).isTrue();
    }

    // TC-UI-ADMIN-03: Open and cancel Create Course modal
    @Test
    @DisplayName("TC-UI-ADMIN-03: 'Add course' opens modal; 'Cancel' closes it")
    void coursesPage_addCourseModal_openAndClose() {
        driver.get(BASE_URL + "/admin/courses");
        waitForElement(By.xpath("//button[contains(text(),'Add course')]")); // wait for page ready
        openCreateCourseModal();

        // Create modal has no guaranteed title text; wait for a modal-only field label
        waitForElement(By.xpath("//label[contains(text(),'Course code')]"));
        assertThat(pageContainsText("Course code")).isTrue();
        assertThat(pageContainsText("Course name")).isTrue();

        // Cancel button closes modal
        WebElement modal = getOpenCreateModal();
        WebElement cancelBtn = modal.findElement(By.xpath(".//button[contains(normalize-space(),'Cancel')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancelBtn);
        wait.until(ExpectedConditions.stalenessOf(modal));
    }

    // TC-UI-ADMIN-04: Create a new course successfully
    @Test
    @DisplayName("TC-UI-ADMIN-04: Creating a course with valid data adds it to the table")
    void coursesPage_createCourse_appearsInTable() {
        driver.get(BASE_URL + "/admin/courses");
        waitForElement(By.xpath("//button[contains(text(),'Add course')]")); // wait for page ready
        openCreateCourseModal();
        waitForElement(By.xpath("//label[contains(text(),'Course code')]"));

        String code = "CS" + (System.currentTimeMillis() % 1_000_000_000L);
        WebElement modal = getOpenCreateModal();
        WebElement codeInput = modal.findElement(By.xpath(".//input[@placeholder='e.g. CS101']"));
        WebElement nameInput = modal.findElement(By.xpath(".//input[@placeholder='e.g. Introduction to Computing']"));
        ((JavascriptExecutor) driver).executeScript(
                "const set = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "set.call(arguments[0], arguments[1]);" +
                        "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));",
                codeInput, code
        );
        ((JavascriptExecutor) driver).executeScript(
                "const set = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "set.call(arguments[0], arguments[1]);" +
                        "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));",
                nameInput, "Test Course"
        );
        assertThat(codeInput.getAttribute("value")).isEqualTo(code);
        assertThat(nameInput.getAttribute("value")).isEqualTo("Test Course");

        // Trigger form submit handler directly to avoid flaky button click behavior in overlay modals.
        WebElement form = modal.findElement(By.xpath(".//form"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));",
                form
        );

        // Successful path is reflected by new code in table; if backend rejects, modal error should appear.
        org.openqa.selenium.support.ui.WebDriverWait createWait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        createWait.until(d ->
                !d.findElements(By.xpath("//div[contains(@class,'fixed') and .//input[@placeholder='e.g. CS101']]")).isEmpty()
                        ? !d.findElements(By.xpath("//*[contains(normalize-space(),'" + code + "')]")).isEmpty()
                            || !d.findElements(By.xpath("//div[contains(@class,'fixed')]//div[contains(@class,'bg-red-50')]")).isEmpty()
                        : true
        );
        java.util.List<WebElement> modalErrors =
                driver.findElements(By.xpath("//div[contains(@class,'fixed')]//div[contains(@class,'bg-red-50')]"));
        assertThat(modalErrors)
                .as("Create course failed: %s", modalErrors.isEmpty() ? "" : modalErrors.get(0).getText())
                .isEmpty();
        waitForText(code);
        assertThat(pageContainsText(code)).isTrue();
    }

    // TC-UI-ADMIN-05: Create course with empty code blocked
    @Test
    @DisplayName("TC-UI-ADMIN-05: Creating course with empty code is blocked by required validation")
    void coursesPage_createCourse_emptyCode_blocked() {
        driver.get(BASE_URL + "/admin/courses");
        waitForElement(By.xpath("//button[contains(text(),'Add course')]")); // wait for page ready
        openCreateCourseModal();
        waitForElement(By.xpath("//label[contains(text(),'Course code')]"));

        // Leave course code blank, fill name only
        waitAndType(By.xpath("//input[@placeholder='e.g. Introduction to Computing']"), "Test");
        waitAndClick(By.xpath("//button[@type='submit' and contains(text(),'Create')]"));

        // Modal stays open — required field not filled
        assertThat(driver.findElements(By.xpath("//label[contains(text(),'Course code')]")).isEmpty()).isFalse();
    }

    // TC-UI-ADMIN-06: Assign instructor modal opens
    @Test
    @DisplayName("TC-UI-ADMIN-06: 'Assign instructor' button opens assignment modal")
    void coursesPage_assignInstructorModal_opens() {
        driver.get(BASE_URL + "/admin/courses");
        waitForElement(By.xpath("//button[contains(text(),'Add course')]"));

        java.util.List<org.openqa.selenium.WebElement> btns =
                driver.findElements(By.xpath("//button[contains(text(),'Assign instructor')]"));
        if (btns.isEmpty()) return; // no courses yet — skip

        openAssignInstructorModal();

        // Wait for modal-specific option text (not table header text)
        waitForElement(By.xpath("//option[contains(text(),'Select an instructor')]"));
        assertThat(pageContainsText("Instructor")).isTrue();

        waitAndClick(By.xpath("//button[contains(text(),'Cancel')]"));
    }

    // TC-UI-ADMIN-07: Students page renders table
    @Test
    @DisplayName("TC-UI-ADMIN-07: Students page loads with table headers")
    void studentsPage_rendersTable() {
        driver.get(BASE_URL + "/admin/students");
        waitForText("Students");
        assertThat(pageContainsText("Name")).isTrue();
        assertThat(pageContainsText("Email")).isTrue();
    }

    // TC-UI-ADMIN-08: Enrollments page loads
    @Test
    @DisplayName("TC-UI-ADMIN-08: Enrollments page loads correctly")
    void enrollmentsPage_renders() {
        driver.get(BASE_URL + "/admin/enrollments");
        waitForText("Enroll");
        assertThat(urlContains("/admin")).isTrue();
    }

    // TC-UI-ADMIN-09: After clearing 'token' and 'user' from localStorage, /admin redirects to /login
    @Test
    @DisplayName("TC-UI-ADMIN-09: After clearing session, /admin redirects to /login")
    void adminRoute_afterLogout_isInaccessible() {
        // AuthContext.jsx stores exactly 'token' and 'user' keys — remove both
        ((JavascriptExecutor) driver).executeScript(
                "localStorage.removeItem('token'); localStorage.removeItem('user');");
        driver.navigate().refresh();

        driver.get(BASE_URL + "/admin/courses");
        waitForUrl("/login");
        assertThat(urlContains("/login")).isTrue();
    }
}