# OEES Selenium UI Tests — Setup Guide

## What's included

| File | Tests | Pages covered |
|---|---|---|
| `BaseTest.java` | (shared setup) | Helper methods, WebDriver lifecycle |
| `AuthUITest.java` | 10 | `/login`, `/register` |
| `AdminUITest.java` | 9 | `/admin/courses`, `/admin/students`, `/admin/enrollments` |
| `InstructorUITest.java` | 12 | `/instructor/questions`, `/instructor/exams`, `/instructor/evaluation` |
| `StudentUITest.java` | 15 | `/student/exams`, `/student/exam/:id`, `/student/results`, `/student/courses` |

**Total: 46 Selenium UI tests**

---

## Prerequisites

### 1. Java 21+
```bash
java -version  # must be 21 or higher
```

### 2. Maven
```bash
mvn -version
```

### 3. Google Chrome browser installed
WebDriverManager (in pom.xml) will **automatically download** the matching ChromeDriver — no manual installation needed.

### 4. Backend running
```bash
cd oees
./gradlew bootRun
# Backend must be at http://localhost:8080
```

### 5. Frontend running
```bash
cd oees-frontend
npm install
npm run dev
# Frontend must be at http://localhost:5173
```

### 6. Seed test accounts in the database
The tests use these fixed accounts — create them via the Register page or Postman first:

| Role       | Email                  | Password      |
|------------|------------------------|---------------|
| ADMIN      | admin@test.com         | password123   |
| INSTRUCTOR | instructor@test.com    | password123   |
| STUDENT    | student@test.com       | password123   |

Make sure the **student is enrolled** in at least one course, and the **instructor is assigned** to a course for the Evaluation and Exams tests to work fully.

---

## Running the tests

### Run all Selenium tests
```bash
cd oees-selenium-tests
mvn test
```

### Run a specific test class
```bash
mvn test -Dtest=AuthUITest
mvn test -Dtest=StudentUITest
mvn test -Dtest=InstructorUITest
mvn test -Dtest=AdminUITest
```

### Run a single test method
```bash
mvn test -Dtest="AuthUITest#login_asStudent_redirectsToStudentDashboard"
```

### Run headlessly (no browser window)
In `BaseTest.java`, uncomment this line:
```java
// opts.addArguments("--headless");
```
Then run `mvn test` as normal.

---

## Project structure
```
oees-selenium-tests/
├── pom.xml
└── src/test/java/com/oees/selenium/
    ├── BaseTest.java           ← shared WebDriver setup & helpers
    ├── AuthUITest.java         ← Login & Register tests (TC-UI-AUTH-*)
    ├── AdminUITest.java        ← Admin page tests (TC-UI-ADMIN-*)
    ├── InstructorUITest.java   ← Instructor page tests (TC-UI-INSTR-*)
    └── StudentUITest.java      ← Student page tests (TC-UI-STU-*)
```

---

## Test ID Reference

### Auth Tests (`AuthUITest.java`)
| ID | Description |
|---|---|
| TC-UI-AUTH-01 | Login page renders OEES branding and form fields |
| TC-UI-AUTH-02 | Admin login redirects to `/admin` |
| TC-UI-AUTH-03 | Instructor login redirects to `/instructor` |
| TC-UI-AUTH-04 | Student login redirects to `/student` |
| TC-UI-AUTH-05 | Wrong password shows inline error |
| TC-UI-AUTH-06 | Empty email blocked by browser validation |
| TC-UI-AUTH-07 | Loading spinner shown during login |
| TC-UI-AUTH-08 | Unauthenticated `/admin` access redirects to `/login` |
| TC-UI-AUTH-09 | Unauthenticated `/student` access redirects to `/login` |
| TC-UI-REG-01  | Register page shows all fields including role dropdown |
| TC-UI-REG-02  | Valid registration redirects to `/login` |
| TC-UI-REG-03  | Duplicate email shows error message |
| TC-UI-REG-04  | Role dropdown has STUDENT, INSTRUCTOR, ADMIN options |
| TC-UI-REG-05  | "Sign in" link navigates to `/login` |

### Admin Tests (`AdminUITest.java`)
| ID | Description |
|---|---|
| TC-UI-ADMIN-01 | Dashboard renders sidebar navigation |
| TC-UI-ADMIN-02 | Courses page shows table headers and Add button |
| TC-UI-ADMIN-03 | Add course modal opens and Cancel closes it |
| TC-UI-ADMIN-04 | Creating a valid course adds it to the table |
| TC-UI-ADMIN-05 | Empty course code blocked by required validation |
| TC-UI-ADMIN-06 | Assign instructor button opens assignment modal |
| TC-UI-ADMIN-07 | Students page loads with table headers |
| TC-UI-ADMIN-08 | Enrollments page loads correctly |
| TC-UI-ADMIN-09 | Admin route inaccessible after logout |

### Instructor Tests (`InstructorUITest.java`)
| ID | Description |
|---|---|
| TC-UI-INSTR-01 | Dashboard renders sidebar navigation |
| TC-UI-INSTR-02 | Question Bank page renders table and Add button |
| TC-UI-INSTR-03 | Add question modal shows MCQ fields by default |
| TC-UI-INSTR-04 | Switching to Descriptive type hides MCQ fields |
| TC-UI-INSTR-05 | Creating valid MCQ question adds to table |
| TC-UI-INSTR-06 | Edit button opens pre-filled modal |
| TC-UI-INSTR-07 | Exams page renders table and Create button |
| TC-UI-INSTR-08 | Create Exam wizard opens at Step 1 |
| TC-UI-INSTR-09 | Continue button disabled with no questions selected |
| TC-UI-INSTR-10 | Close (X) button dismisses wizard |
| TC-UI-INSTR-11 | Evaluation page renders Course dropdown and panels |
| TC-UI-INSTR-12 | Selecting an exam shows Submitted Attempts panel |

### Student Tests (`StudentUITest.java`)
| ID | Description |
|---|---|
| TC-UI-STU-01 | Dashboard renders sidebar with navigation links |
| TC-UI-STU-02 | My Courses page loads with course list or empty state |
| TC-UI-STU-03 | Available Exams page loads with title and subtitle |
| TC-UI-STU-04 | Exam cards show duration, marks, pass mark, question count |
| TC-UI-STU-05 | Start Exam button disabled for non-ACTIVE exams |
| TC-UI-STU-06 | Empty state message shown when no exams available |
| TC-UI-STU-07 | TakeExam header shows Timer and Answered counter |
| TC-UI-STU-08 | TakeExam shows numbered question navigation sidebar |
| TC-UI-STU-09 | MCQ question renders A/B/C/D radio options |
| TC-UI-STU-10 | Previous button disabled on Q1; Next navigates forward |
| TC-UI-STU-11 | Submit button triggers browser confirmation dialog |
| TC-UI-STU-12 | Timer element uses correct CSS class for countdown |
| TC-UI-STU-13 | Results page loads with heading and exam selector |
| TC-UI-STU-14 | Results page shows Grade, Rank, and Class avg |
| TC-UI-STU-15 | Student accessing `/instructor` route redirected to `/login` |
