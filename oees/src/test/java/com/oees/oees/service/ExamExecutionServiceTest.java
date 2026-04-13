package com.oees.oees.service;

import com.oees.oees.dto.request.SubmitAttemptRequest;
import com.oees.oees.dto.response.AttemptResponse;
import com.oees.oees.entity.*;
import com.oees.oees.enums.AttemptStatus;
import com.oees.oees.enums.ExamStatus;
import com.oees.oees.enums.QuestionType;
import com.oees.oees.enums.DifficultyLevel;
import com.oees.oees.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExamExecutionService Tests")
class ExamExecutionServiceTest {

    @Mock private ExamRepository examRepository;
    @Mock private ExamQuestionRepository examQuestionRepository;
    @Mock private StudentAttemptRepository attemptRepository;
    @Mock private StudentResponseRepository responseRepository;
    @Mock private userRepository userRepo;
    @Mock private QuestionRepository questionRepository;

    @InjectMocks private ExamExecutionService service;

    private Exam activeExam;
    private User student;
    private Question mcqQuestion;
    private ExamQuestion examQuestion;

    @BeforeEach
    void setUp() {
        Course course = new Course();
        course.setId(1L);
        course.setCourseName("Software Engineering");

        activeExam = Exam.builder()
                .id(10L)
                .title("Mid-term Exam")
                .course(course)
                .durationMinutes(60)
                .totalMarks(100)
                .maxAttempts(2)
                .passMark(50)
                .status(ExamStatus.ACTIVE)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        student = User.builder()
                .id(5L)
                .name("Alice")
                .email("alice@example.com")
                .role(com.oees.oees.enums.Role.STUDENT)
                .active(true)
                .build();

        mcqQuestion = Question.builder()
                .id(100L)
                .content("What is JPA?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .difficultyLevel(DifficultyLevel.EASY)
                .marks(5)
                .unit("Unit1")
                .optionA("Java Persistence API")
                .optionB("Java Process API")
                .optionC("Java Package Abstraction")
                .optionD("None")
                .correctAnswer("Java Persistence API")
                .build();

        examQuestion = ExamQuestion.builder()
                .id(1L)
                .exam(activeExam)
                .question(mcqQuestion)
                .marks(5)
                .orderIndex(1)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-ATTEMPT-01: Start attempt on an active exam
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-ATTEMPT-01: Start attempt on active exam returns questions with deadline")
    void startAttempt_activeExam_returnsAttemptResponse() {
        StudentAttempt savedAttempt = StudentAttempt.builder()
                .id(1L).exam(activeExam).student(student)
                .startedAt(LocalDateTime.now()).attemptNumber(1)
                .status(AttemptStatus.IN_PROGRESS).build();

        when(examRepository.findById(10L)).thenReturn(Optional.of(activeExam));
        when(attemptRepository.countByExamIdAndStudentId(10L, 5L)).thenReturn(0);
        when(attemptRepository.findByExamIdAndStudentIdAndStatus(10L, 5L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(userRepo.findById(5L)).thenReturn(Optional.of(student));
        when(attemptRepository.save(any())).thenReturn(savedAttempt);
        when(examQuestionRepository.findByExamIdOrderByOrderIndex(10L))
                .thenReturn(List.of(examQuestion));

        AttemptResponse response = service.startAttempt(10L, 5L);

        assertThat(response.getAttemptId()).isEqualTo(1L);
        assertThat(response.getQuestions()).hasSize(1);
        assertThat(response.getDeadline()).isAfter(response.getStartedAt());
        assertThat(response.getQuestions().get(0).getOptions()).hasSize(4);
    }

    // ─────────────────────────────────────────────────────────────
    // TC-ATTEMPT-02: Start attempt on a non-active (DRAFT) exam
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-ATTEMPT-02: Start attempt on DRAFT exam throws RuntimeException")
    void startAttempt_draftExam_throwsException() {
        activeExam.setStatus(ExamStatus.DRAFT);
        when(examRepository.findById(10L)).thenReturn(Optional.of(activeExam));

        assertThatThrownBy(() -> service.startAttempt(10L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Exam is not currently active");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-ATTEMPT-03: Start attempt when max attempts exceeded
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-ATTEMPT-03: Exceed max attempt limit throws RuntimeException")
    void startAttempt_maxAttemptsReached_throwsException() {
        when(examRepository.findById(10L)).thenReturn(Optional.of(activeExam));
        when(attemptRepository.countByExamIdAndStudentId(10L, 5L)).thenReturn(2); // maxAttempts=2

        assertThatThrownBy(() -> service.startAttempt(10L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Maximum attempt limit reached");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-ATTEMPT-04: Start attempt when one is already in progress
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-ATTEMPT-04: Starting duplicate in-progress attempt throws RuntimeException")
    void startAttempt_alreadyInProgress_throwsException() {
        StudentAttempt inProgress = StudentAttempt.builder()
                .id(99L).status(AttemptStatus.IN_PROGRESS).build();

        when(examRepository.findById(10L)).thenReturn(Optional.of(activeExam));
        when(attemptRepository.countByExamIdAndStudentId(10L, 5L)).thenReturn(0);
        when(attemptRepository.findByExamIdAndStudentIdAndStatus(10L, 5L, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.of(inProgress));

        assertThatThrownBy(() -> service.startAttempt(10L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Active attempt already exists");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-ATTEMPT-05: Start attempt on non-existent exam
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-ATTEMPT-05: Start attempt for unknown examId throws RuntimeException")
    void startAttempt_unknownExam_throwsException() {
        when(examRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startAttempt(999L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Exam not found");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-SUBMIT-01: Submit an in-progress MCQ attempt — auto-scored
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-SUBMIT-01: Submit MCQ attempt auto-evaluates and returns score")
    void submitAttempt_mcqOnly_autoEvaluatesAndReturnsScore() {
        StudentAttempt attempt = StudentAttempt.builder()
                .id(1L).exam(activeExam).student(student)
                .status(AttemptStatus.IN_PROGRESS).build();

        SubmitAttemptRequest req = new SubmitAttemptRequest();
        SubmitAttemptRequest.ResponseItem item = new SubmitAttemptRequest.ResponseItem();
        item.setQuestionId(100L);
        item.setSelectedOption("Java Persistence API");
        req.setResponses(List.of(item));

        StudentResponse sr = StudentResponse.builder()
                .id(1L).attempt(attempt).question(mcqQuestion)
                .selectedOption("Java Persistence API")
                .marksAwarded(5).build();

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(100L)).thenReturn(Optional.of(mcqQuestion));
        when(responseRepository.saveAll(anyList())).thenReturn(List.of());
        when(attemptRepository.save(any())).thenReturn(attempt);
        when(responseRepository.findByAttemptId(1L)).thenReturn(List.of(sr));

        ExamExecutionService.SubmitResult result = service.submitAttempt(1L, 5L, req);

        assertThat(result.score()).isEqualTo(5);
        assertThat(result.totalMarks()).isEqualTo(100);
        assertThat(result.pendingEvaluation()).isFalse();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-SUBMIT-02: Submit already-submitted attempt
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-SUBMIT-02: Re-submitting a SUBMITTED attempt throws RuntimeException")
    void submitAttempt_alreadySubmitted_throwsException() {
        StudentAttempt attempt = StudentAttempt.builder()
                .id(1L).exam(activeExam).student(student)
                .status(AttemptStatus.SUBMITTED).build();

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(1L, 5L, new SubmitAttemptRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Attempt already submitted");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-SUBMIT-03: Student submitting another student's attempt
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-SUBMIT-03: Submit attempt owned by different student throws Unauthorized")
    void submitAttempt_wrongStudent_throwsException() {
        User otherStudent = User.builder().id(99L).build();
        StudentAttempt attempt = StudentAttempt.builder()
                .id(1L).exam(activeExam).student(otherStudent)
                .status(AttemptStatus.IN_PROGRESS).build();

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submitAttempt(1L, 5L, new SubmitAttemptRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unauthorized");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-SUBMIT-04: Correct MCQ answer gets full marks in auto-eval
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-SUBMIT-04: autoEvaluateObjective gives full marks for correct MCQ answer")
    void autoEvaluate_correctMcqAnswer_awardsFullMarks() {
        StudentAttempt attempt = StudentAttempt.builder()
                .id(1L).exam(activeExam).student(student)
                .status(AttemptStatus.IN_PROGRESS).build();

        StudentResponse sr = StudentResponse.builder()
                .id(1L).attempt(attempt).question(mcqQuestion)
                .selectedOption("Java Persistence API").build();

        when(responseRepository.findByAttemptId(1L)).thenReturn(List.of(sr));
        when(responseRepository.saveAll(anyList())).thenReturn(List.of(sr));
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(attemptRepository.save(any())).thenReturn(attempt);

        service.autoEvaluateObjective(1L);

        assertThat(sr.getMarksAwarded()).isEqualTo(5);
    }

    // ─────────────────────────────────────────────────────────────
    // TC-SUBMIT-05: Wrong MCQ answer gets zero marks
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-SUBMIT-05: autoEvaluateObjective gives 0 for wrong MCQ answer")
    void autoEvaluate_wrongMcqAnswer_awardsZero() {
        StudentAttempt attempt = StudentAttempt.builder()
                .id(1L).exam(activeExam).student(student)
                .status(AttemptStatus.IN_PROGRESS).build();

        StudentResponse sr = StudentResponse.builder()
                .id(1L).attempt(attempt).question(mcqQuestion)
                .selectedOption("Java Process API").build(); // wrong answer

        when(responseRepository.findByAttemptId(1L)).thenReturn(List.of(sr));
        when(responseRepository.saveAll(anyList())).thenReturn(List.of(sr));
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(attemptRepository.save(any())).thenReturn(attempt);

        service.autoEvaluateObjective(1L);

        assertThat(sr.getMarksAwarded()).isEqualTo(0);
    }
}
