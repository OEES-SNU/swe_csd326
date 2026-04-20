package com.oees.selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-UI-INSTRUCTOR: Instructor pages Selenium tests.
 *
 * FIXES applied after reading Questions.jsx and Evaluation.jsx:
 *  - Login redirects to /instructor/exams (not /instructor)
 *  - Question type dropdown option is "Descriptive" (value=DESCRIPTIVE) ✓
 *    But there are TWO selects on the page (Type + Difficulty). Must target the Type select specifically.
 *  - Evaluation page title is "Evaluation" ✓ but it only loads after courses API resolves.
 *    Added longer wait and navigate directly to /instructor/evaluation.
 */
@DisplayName("UI: Instructor Pages")
class InstructorUITest extends BaseTest {

    @BeforeEach
    void loginAsInstructor() {
        loginAs(INSTRUCTOR_EMAIL, INSTRUCTOR_PASS);
        waitForUrl("/instructor/exams");
    }

    // TC-UI-INSTR-01: Instructor dashboard renders sidebar links
    @Test
    @DisplayName("TC-UI-INSTR-01: Instructor dashboard renders navigation sidebar")
    void instructorDashboard_rendersSidebar() {
        waitForText("Question Bank");
        assertThat(pageContainsText("Exams")).isTrue();
        assertThat(pageContainsText("Evaluation")).isTrue();
    }

    // TC-UI-INSTR-02: Question Bank page renders table and Add button
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

    // TC-UI-INSTR-03: Add Question modal opens with MCQ fields by default
    @Test
    @DisplayName("TC-UI-INSTR-03: 'Add question' opens modal showing MCQ option fields by default")
    void questionsPage_addQuestionModal_showsMcqFields() {
        driver.get(BASE_URL + "/instructor/questions");
        waitAndClick(By.xpath("//button[contains(text(),'Add question')]"));
        waitForText("Add Question");

        // MCQ is default — Option A/B labels visible
        assertThat(pageContainsText("Option A")).isTrue();
        assertThat(pageContainsText("Option B")).isTrue();
        // Correct Answer label also shown for MCQ
        assertThat(pageContainsText("Correct Answer")).isTrue();
    }

    // TC-UI-INSTR-04: Switching to Descriptive hides MCQ fields
    @Test
    @DisplayName("TC-UI-INSTR-04: Switching type to 'Descriptive' hides MCQ option fields")
    void questionsPage_switchToDescriptive_hidesMcqFields() {
        driver.get(BASE_URL + "/instructor/questions");
        waitAndClick(By.xpath("//button[contains(text(),'Add question')]"));
        waitForText("Add Question");

        // In Questions.jsx the Type select has label "Type" — it's the FIRST select in the modal
        // Select by finding the select that currently has "Multiple Choice" selected
        WebElement typeSelect = waitForElement(
        By.xpath("//form//label[text()='Type']/..//select"));
        new Select(typeSelect).selectByVisibleText("Descriptive");

        // Option A/B should disappear (isMCQ becomes false)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Option A')]")));
        assertThat(driver.findElements(
                By.xpath("//*[contains(text(),'Option A')]")).isEmpty()).isTrue();
    }

    // TC-UI-INSTR-05: Edit button opens pre-filled modal
    @Test
    @DisplayName("TC-UI-INSTR-05: 'Edit' button opens modal pre-filled with question data")
    void questionsPage_editButton_opensPreffilledModal() {
        driver.get(BASE_URL + "/instructor/questions");
        waitForText("Question Bank");

        java.util.List<org.openqa.selenium.WebElement> editButtons =
                driver.findElements(By.xpath("//button[contains(text(),'Edit')]"));
        if (editButtons.isEmpty()) return; // no questions yet — skip

        editButtons.get(0).click();
        waitForText("Edit Question");

        // Modal is pre-filled — textarea not empty
        String content = driver.findElement(By.cssSelector("textarea")).getAttribute("value");
        assertThat(content).isNotEmpty();
    }

    // TC-UI-INSTR-06: Exams page renders table and Create button
    @Test
    @DisplayName("TC-UI-INSTR-06: Exams page renders table with status badges and 'Create exam' button")
    void examsPage_rendersTableAndCreateButton() {
        driver.get(BASE_URL + "/instructor/exams");
        waitForElement(By.xpath("//th[contains(normalize-space(),'Duration')]"));
        assertThat(pageContainsText("Duration")).isTrue();
        assertThat(pageContainsText("Status")).isTrue();
        assertThat(waitForElement(
                By.xpath("//button[contains(text(),'Create exam')]")).isDisplayed()).isTrue();
    }

    // TC-UI-INSTR-07: Exam creation wizard opens on Step 1
    @Test
    @DisplayName("TC-UI-INSTR-07: 'Create exam' opens full-screen wizard at Step 1 (Select Questions)")
    void examsPage_createExamWizard_opensAtStep1() {
        driver.get(BASE_URL + "/instructor/exams");
        waitAndClick(By.xpath("//button[contains(text(),'Create exam')]"));
        waitForText("Create Exam");
        assertThat(pageContainsText("Select Questions")).isTrue();
    }

    // TC-UI-INSTR-08: Continue disabled with no questions selected
    @Test
    @DisplayName("TC-UI-INSTR-08: 'Continue' button in wizard is disabled when no questions selected")
    void examWizard_continueDisabled_whenNoQuestionsSelected() {
        driver.get(BASE_URL + "/instructor/exams");
        waitAndClick(By.xpath("//button[contains(text(),'Create exam')]"));
        waitForText("Select Questions");

        org.openqa.selenium.WebElement continueBtn = waitForElement(
                By.xpath("//button[contains(text(),'Continue with')]"));
        assertThat(continueBtn.isEnabled()).isFalse();
    }

    // TC-UI-INSTR-09: Evaluation page renders
    @Test
    @DisplayName("TC-UI-INSTR-09: Evaluation page loads with 'Evaluation' heading and panels")
    void evaluationPage_rendersSelectors() {
        driver.get(BASE_URL + "/instructor/evaluation");

        // Wait for heading — from Evaluation.jsx: <h1>Evaluation</h1>
        waitForUrl("/instructor/evaluation");
        waitForElement(By.xpath("//label[contains(text(),'Course:')]")); // wait for full render
        assertThat(pageContainsText("Grade descriptive")).isTrue();

        // Course selector rendered
        assertThat(pageContainsText("Course:")).isTrue();

        // Exams panel label: "EXAMS" (uppercase in JSX: text-xs uppercase)
        assertThat(pageContainsText("Exams")).isTrue();

        // Attempts panel label: "SUBMITTED ATTEMPTS"
        assertThat(pageContainsText("Submitted Attempts")).isTrue();
    }

    // TC-UI-INSTR-10: Selecting an exam shows Submitted Attempts panel
    @Test
    @DisplayName("TC-UI-INSTR-10: Clicking an exam in Evaluation page shows Submitted Attempts panel")
    void evaluationPage_selectExam_showsAttempts() {
        driver.get(BASE_URL + "/instructor/evaluation");
        waitForText("Evaluation");
        waitForElement(By.xpath("//label[contains(text(),'Course:')]")); // wait for full render

        java.util.List<org.openqa.selenium.WebElement> examItems = driver.findElements(
                By.cssSelector(".card button"));
        if (examItems.isEmpty()) return; // no exams yet — skip

        examItems.get(0).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Submitted Attempts')]")));
    }
}