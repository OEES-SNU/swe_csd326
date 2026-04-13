package com.oees.oees.service;

import com.oees.oees.dto.response.ResultResponse;
import com.oees.oees.entity.*;
import com.oees.oees.enums.AttemptStatus;
import com.oees.oees.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResultService Tests")
class ResultServiceTest {

    @Mock private StudentAttemptRepository attemptRepository;
    @Mock private StudentResponseRepository responseRepository;
    @Mock private ExamResultRepository resultRepository;
    @Mock private ExamRepository examRepository;

    @InjectMocks private ResultService resultService;

    private Exam exam;
    private User student;
    private StudentAttempt evaluatedAttempt;

    @BeforeEach
    void setUp() {
        Course course = new Course();
        course.setId(1L);
        course.setCourseName("SWE");

        exam = Exam.builder()
                .id(10L).title("Final Exam")
                .course(course).totalMarks(100).passMark(50)
                .build();

        student = User.builder()
                .id(5L).name("Alice").email("alice@example.com")
                .role(com.oees.oees.enums.Role.STUDENT).active(true).build();

        evaluatedAttempt = StudentAttempt.builder()
                .id(1L).exam(exam).student(student)
                .status(AttemptStatus.EVALUATED).totalScore(75)
                .attemptNumber(1).build();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-RESULT-01: Generate results when all attempts are evaluated
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RESULT-01: generateResults saves ExamResult with grade and rank")
    void generateResults_allEvaluated_savesResult() {
        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.EVALUATED))
                .thenReturn(List.of(evaluatedAttempt));
        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.SUBMITTED))
                .thenReturn(List.of());
        when(examRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(responseRepository.findByAttemptId(1L)).thenReturn(List.of(
                StudentResponse.builder().marksAwarded(75).build()
        ));
        when(resultRepository.findByAttemptId(1L)).thenReturn(Optional.empty());
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        resultService.generateResults(10L);

        verify(resultRepository).save(argThat(r ->
                r.getTotalMarks() == 75 &&
                r.getGrade().equals("B") &&
                r.getPassed() &&
                r.getRank() == 1));
    }

    // ─────────────────────────────────────────────────────────────
    // TC-RESULT-02: Generate results when some attempts still SUBMITTED
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RESULT-02: generateResults throws if any attempt is still SUBMITTED")
    void generateResults_pendingAttempts_throwsException() {
        StudentAttempt submittedAttempt = StudentAttempt.builder()
                .id(2L).exam(exam).student(student)
                .status(AttemptStatus.SUBMITTED).build();

        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.EVALUATED))
                .thenReturn(List.of(evaluatedAttempt));
        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.SUBMITTED))
                .thenReturn(List.of(submittedAttempt));

        assertThatThrownBy(() -> resultService.generateResults(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not all attempts are evaluated yet");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-RESULT-03: Grade boundaries — 90%+ should be A+
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RESULT-03: Score 90/100 earns grade A+")
    void generateResults_score90_gradeAPlus() {
        evaluatedAttempt.setTotalScore(90);
        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.EVALUATED))
                .thenReturn(List.of(evaluatedAttempt));
        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.SUBMITTED))
                .thenReturn(List.of());
        when(examRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(responseRepository.findByAttemptId(1L)).thenReturn(List.of(
                StudentResponse.builder().marksAwarded(90).build()
        ));
        when(resultRepository.findByAttemptId(1L)).thenReturn(Optional.empty());
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        resultService.generateResults(10L);

        verify(resultRepository).save(argThat(r -> r.getGrade().equals("A+")));
    }

    // ─────────────────────────────────────────────────────────────
    // TC-RESULT-04: Score below passMark — passed=false
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RESULT-04: Score below passMark results in passed=false")
    void generateResults_belowPassMark_passedFalse() {
        evaluatedAttempt.setTotalScore(40); // passMark=50
        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.EVALUATED))
                .thenReturn(List.of(evaluatedAttempt));
        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.SUBMITTED))
                .thenReturn(List.of());
        when(examRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(responseRepository.findByAttemptId(1L)).thenReturn(List.of(
                StudentResponse.builder().marksAwarded(40).build()
        ));
        when(resultRepository.findByAttemptId(1L)).thenReturn(Optional.empty());
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        resultService.generateResults(10L);

        verify(resultRepository).save(argThat(r -> !r.getPassed()));
    }

    // ─────────────────────────────────────────────────────────────
    // TC-RESULT-05: Get student result returns correct ResultResponse
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RESULT-05: getStudentResult returns grade, rank, and exam average")
    void getStudentResult_existingResult_returnsResponse() {
        ExamResult examResult = ExamResult.builder()
                .id(1L).attempt(evaluatedAttempt).student(student).exam(exam)
                .totalMarks(75).grade("B").passed(true).rank(1).build();

        when(attemptRepository.findByStudentId(5L)).thenReturn(List.of(evaluatedAttempt));
        when(resultRepository.findByAttemptId(1L)).thenReturn(Optional.of(examResult));
        when(resultRepository.findAverageMarksByExamId(10L)).thenReturn(68.0);

        ResultResponse response = resultService.getStudentResult(10L, 5L);

        assertThat(response.getExamId()).isEqualTo(10L);
        assertThat(response.getTotalMarks()).isEqualTo(75);
        assertThat(response.getGrade()).isEqualTo("B");
        assertThat(response.getPassed()).isTrue();
        assertThat(response.getRank()).isEqualTo(1);
        assertThat(response.getExamAverage()).isEqualTo(68.0);
    }

    // ─────────────────────────────────────────────────────────────
    // TC-RESULT-06: Get result when result not generated yet
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RESULT-06: getStudentResult before generate throws RuntimeException")
    void getStudentResult_notGenerated_throwsException() {
        when(attemptRepository.findByStudentId(5L)).thenReturn(List.of(evaluatedAttempt));
        when(resultRepository.findByAttemptId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultService.getStudentResult(10L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Result not generated yet");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-RESULT-07: Get result when student has no attempt
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RESULT-07: getStudentResult for student with no attempt throws RuntimeException")
    void getStudentResult_noAttempt_throwsException() {
        when(attemptRepository.findByStudentId(5L)).thenReturn(List.of());

        assertThatThrownBy(() -> resultService.getStudentResult(10L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Result not available yet");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-RESULT-08: Multiple students — ranks assigned in score order
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-RESULT-08: Two students ranked by score descending")
    void generateResults_twoStudents_rankedByScore() {
        User student2 = User.builder().id(6L).name("Bob").role(com.oees.oees.enums.Role.STUDENT).active(true).build();
        StudentAttempt attempt2 = StudentAttempt.builder()
                .id(2L).exam(exam).student(student2)
                .status(AttemptStatus.EVALUATED).totalScore(90).build();

        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.EVALUATED))
                .thenReturn(List.of(evaluatedAttempt, attempt2));
        when(attemptRepository.findByExamIdAndStatus(10L, AttemptStatus.SUBMITTED))
                .thenReturn(List.of());
        when(examRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(responseRepository.findByAttemptId(1L)).thenReturn(List.of(
                StudentResponse.builder().marksAwarded(75).build()));
        when(responseRepository.findByAttemptId(2L)).thenReturn(List.of(
                StudentResponse.builder().marksAwarded(90).build()));
        when(resultRepository.findByAttemptId(anyLong())).thenReturn(Optional.empty());
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        resultService.generateResults(10L);

        // Bob (90 marks) should be rank 1, Alice (75 marks) should be rank 2
        verify(resultRepository).save(argThat(r ->
                r.getStudent().getId().equals(6L) && r.getRank() == 1));
        verify(resultRepository).save(argThat(r ->
                r.getStudent().getId().equals(5L) && r.getRank() == 2));
    }
}
