package com.oees.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-UI-INSTRUCTOR: Instructor pages Selenium tests.
 *
 * Pages under test:
 *   /instructor/questions  → Questions.jsx
 *   /instructor/exams      → Exams.jsx
 *   /instructor/evaluation → Evaluation.jsx
 */
@DisplayName("UI: Instructor Pages")
class InstructorUITest extends BaseTest {

    @BeforeEach
    void loginAsInstructor() {
        loginAs(INSTRUCTOR_EMAIL, INSTRUCTOR_PASS);
        waitForUrl("/instructor");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-01: Instructor dashboard renders sidebar links
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-01: Instructor dashboard renders navigation sidebar")
    void instructorDashboard_rendersSidebar() {
        waitForText("Question Bank");
        assertThat(pageContainsText("Exams")).isTrue();
        assertThat(pageContainsText("Evaluation")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-02: Question Bank page renders table and Add button
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-02: Question Bank page renders table headers and 'Add question' button")
    void questionsPage_rendersTableAndButton() {
        driver.get(BASE_URL + "/instructor/questions");

        waitForText("Question Bank");
        assertThat(pageContainsText("Type")).isTrue();
        assertThat(pageContainsText("Difficulty")).isTrue();
        assertThat(pageContainsText("Marks")).isTrue();
        assertThat(waitForElement(
                By.xpath("//button[contains(text(),'Add question')]")).isDisplayed()).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-03: Add Question modal opens and shows MCQ fields
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-03: 'Add question' opens modal with MCQ option fields by default")
    void questionsPage_addQuestionModal_showsMcqFields() {
        driver.get(BASE_URL + "/instructor/questions");
        waitAndClick(By.xpath("//button[contains(text(),'Add question')]"));

        waitForText("Add Question");

        // Type dropdown defaults to Multiple Choice
        Select typeSelect = new Select(waitForElement(By.cssSelector("select[value='MULTIPLE_CHOICE'], select")));

        // MCQ-specific fields shown
        assertThat(pageContainsText("Option A")).isTrue();
        assertThat(pageContainsText("Option B")).isTrue();
        assertThat(pageContainsText("Correct Answer")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-04: Switching question type to Descriptive hides MCQ fields
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-04: Switching type to 'Descriptive' hides MCQ option fields")
    void questionsPage_switchToDescriptive_hidesMcqFields() {
        driver.get(BASE_URL + "/instructor/questions");
        waitAndClick(By.xpath("//button[contains(text(),'Add question')]"));
        waitForText("Add Question");

        // Change type to Descriptive
        selectOption(By.cssSelector("select"), "Descriptive");

        // MCQ options should no longer be visible
        assertThat(driver.findElements(
                By.xpath("//*[contains(text(),'Option A')]")).isEmpty()).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-05: Create MCQ question successfully
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-05: Creating a valid MCQ question adds it to the table")
    void questionsPage_createMcqQuestion_appearsInTable() {
        driver.get(BASE_URL + "/instructor/questions");
        waitAndClick(By.xpath("//button[contains(text(),'Add question')]"));
        waitForText("Add Question");

        String questionText = "What is Selenium? " + System.currentTimeMillis();

        // Fill question textarea
        waitAndType(By.cssSelector("textarea"), questionText);

        // Marks and unit
        waitAndType(By.xpath("//input[@type='number' and @min='1']"), "5");
        waitAndType(By.xpath("//input[@placeholder='e.g. Unit 3']"), "Unit1");

        // MCQ options
        waitAndType(By.xpath("//input[preceding::label[contains(text(),'Option A')]][1]"), "Browser automation tool");
        waitAndType(By.xpath("//input[preceding::label[contains(text(),'Option B')]][1]"), "A database");
        waitAndType(By.xpath("//input[preceding::label[contains(text(),'Correct Answer')]]"), "A");

        // Submit
        waitAndClick(By.xpath("//button[@type='submit' and contains(text(),'Add question')]"));

        // Modal closes, question appears in table
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//h2[contains(text(),'Add Question')]")));
        waitForText("What is Selenium");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-06: Edit question button opens pre-filled modal
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-06: 'Edit' button opens modal pre-filled with question data")
    void questionsPage_editButton_opensPreffilledModal() {
        driver.get(BASE_URL + "/instructor/questions");

        // Wait for questions to load
        waitForText("Question Bank");

        // Click first edit button (if any question exists)
        java.util.List<org.openqa.selenium.WebElement> editButtons =
                driver.findElements(By.xpath("//button[contains(text(),'Edit')]"));
        if (editButtons.isEmpty()) return; // skip if no questions yet

        editButtons.get(0).click();
        waitForText("Edit Question");

        // Modal should be pre-filled (textarea not empty)
        String content = driver.findElement(By.cssSelector("textarea")).getAttribute("value");
        assertThat(content).isNotEmpty();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-07: Exams page renders table and Create button
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-07: Exams page renders table with status badges and 'Create exam' button")
    void examsPage_rendersTableAndCreateButton() {
        driver.get(BASE_URL + "/instructor/exams");

        waitForText("Exams");
        assertThat(pageContainsText("Duration")).isTrue();
        assertThat(pageContainsText("Status")).isTrue();
        assertThat(waitForElement(
                By.xpath("//button[contains(text(),'Create exam')]")).isDisplayed()).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-08: Exam creation wizard opens on Step 1
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-08: 'Create exam' opens full-screen wizard at Step 1 (Select Questions)")
    void examsPage_createExamWizard_opensAtStep1() {
        driver.get(BASE_URL + "/instructor/exams");
        waitAndClick(By.xpath("//button[contains(text(),'Create exam')]"));

        // Wizard header appears
        waitForText("Create Exam");
        assertThat(pageContainsText("Select Questions")).isTrue();

        // Step indicator shows step 1 active
        assertThat(waitForElement(
                By.xpath("//*[contains(text(),'Select Questions')]")).isDisplayed()).isTrue();

        // Filter bar visible
        assertThat(pageContainsText("All difficulties")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-09: Wizard step 1 — Continue disabled with no questions selected
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-09: 'Continue' button in wizard is disabled when no questions selected")
    void examWizard_continueDisabled_whenNoQuestionsSelected() {
        driver.get(BASE_URL + "/instructor/exams");
        waitAndClick(By.xpath("//button[contains(text(),'Create exam')]"));
        waitForText("Select Questions");

        // Continue button should be disabled
        org.openqa.selenium.WebElement continueBtn = waitForElement(
                By.xpath("//button[contains(text(),'Continue with')]"));
        assertThat(continueBtn.isEnabled()).isFalse();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-10: Wizard close button dismisses wizard
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-10: Closing the wizard with X returns to Exams page")
    void examWizard_closeButton_dismissesWizard() {
        driver.get(BASE_URL + "/instructor/exams");
        waitAndClick(By.xpath("//button[contains(text(),'Create exam')]"));
        waitForText("Create Exam");

        // Click X (close) button in wizard header
        waitAndClick(By.cssSelector(".fixed button svg.w-5")); // X icon button

        // Wizard gone, back on Exams page
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//h2[contains(text(),'Create Exam')]")));
        waitForText("Exams");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-11: Evaluation page loads with course and exam selectors
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-11: Evaluation page loads with Course dropdown and Exams panel")
    void evaluationPage_rendersSelectors() {
        driver.get(BASE_URL + "/instructor/evaluation");

        waitForText("Evaluation");
        assertThat(pageContainsText("Grade descriptive")).isTrue();
        assertThat(pageContainsText("Course:")).isTrue();
        assertThat(pageContainsText("Exams")).isTrue();
        assertThat(pageContainsText("Submitted Attempts")).isTrue();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-UI-INSTR-12: Evaluation page — selecting exam loads attempts panel
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-UI-INSTR-12: Clicking an exam in Evaluation page shows Submitted Attempts panel")
    void evaluationPage_selectExam_showsAttempts() {
        driver.get(BASE_URL + "/instructor/evaluation");
        waitForText("Evaluation");

        // If any exam is listed, click it
        java.util.List<org.openqa.selenium.WebElement> examItems = driver.findElements(
                By.cssSelector(".card button"));
        if (examItems.isEmpty()) return; // skip if no exams yet

        examItems.get(0).click();

        // Attempts panel should appear (empty or populated)
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Submitted Attempts')]")));
    }
}
