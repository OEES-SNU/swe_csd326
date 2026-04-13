package com.oees.oees.service;

import com.oees.oees.dto.request.EvaluateResponseRequest;
import com.oees.oees.entity.*;
import com.oees.oees.enums.AttemptStatus;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluationService Tests")
class EvaluationServiceTest {

    @Mock private StudentResponseRepository responseRepository;
    @Mock private StudentAttemptRepository attemptRepository;
    @Mock private userRepository userRepo;

    @InjectMocks private EvaluationService evaluationService;

    private Question descriptiveQuestion;
    private Question mcqQuestion;
    private StudentAttempt attempt;
    private User instructor;

    @BeforeEach
    void setUp() {
        instructor = User.builder().id(2L).name("Prof. Smith")
                .role(com.oees.oees.enums.Role.INSTRUCTOR).active(true).build();

        descriptiveQuestion = Question.builder()
                .id(200L).content("Explain SOLID principles.")
                .type(QuestionType.DESCRIPTIVE)
                .difficultyLevel(DifficultyLevel.HARD)
                .marks(10).unit("Unit2").build();

        mcqQuestion = Question.builder()
                .id(100L).content("What is OOP?")
                .type(QuestionType.MULTIPLE_CHOICE)
                .marks(5).build();

        attempt = StudentAttempt.builder()
                .id(1L).status(AttemptStatus.SUBMITTED)
                .responses(new ArrayList<>()).build();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-EVAL-01: Grade a valid descriptive response
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EVAL-01: Grade descriptive response with valid marks saves correctly")
    void evaluateResponse_validDescriptive_savesMarks() {
        StudentResponse sr = StudentResponse.builder()
                .id(1L).attempt(attempt).question(descriptiveQuestion)
                .responseText("SOLID stands for...").build();
        attempt.getResponses().add(sr);

        EvaluateResponseRequest req = new EvaluateResponseRequest();
        req.setResponseId(1L);
        req.setMarksAwarded(8);

        when(responseRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepo.findById(2L)).thenReturn(Optional.of(instructor));
        when(responseRepository.save(any())).thenReturn(sr);
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(attemptRepository.save(any())).thenReturn(attempt);

        evaluationService.evaluateResponse(req, 2L);

        assertThat(sr.getMarksAwarded()).isEqualTo(8);
        assertThat(sr.getEvaluatedBy()).isEqualTo(instructor);
        assertThat(sr.getEvaluatedAt()).isNotNull();
    }

    // ─────────────────────────────────────────────────────────────
    // TC-EVAL-02: Grade marks exceeding question max
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EVAL-02: Marks above question max throws RuntimeException")
    void evaluateResponse_marksAboveMax_throwsException() {
        StudentResponse sr = StudentResponse.builder()
                .id(1L).attempt(attempt).question(descriptiveQuestion).build();

        EvaluateResponseRequest req = new EvaluateResponseRequest();
        req.setResponseId(1L);
        req.setMarksAwarded(15); // max is 10

        when(responseRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepo.findById(2L)).thenReturn(Optional.of(instructor));

        assertThatThrownBy(() -> evaluationService.evaluateResponse(req, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Marks must be between 0 and 10");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-EVAL-03: Grade with negative marks
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EVAL-03: Negative marks throws RuntimeException")
    void evaluateResponse_negativeMarks_throwsException() {
        StudentResponse sr = StudentResponse.builder()
                .id(1L).attempt(attempt).question(descriptiveQuestion).build();

        EvaluateResponseRequest req = new EvaluateResponseRequest();
        req.setResponseId(1L);
        req.setMarksAwarded(-1);

        when(responseRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepo.findById(2L)).thenReturn(Optional.of(instructor));

        assertThatThrownBy(() -> evaluationService.evaluateResponse(req, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Marks must be between 0 and");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-EVAL-04: Attempt to grade an MCQ response (not allowed)
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EVAL-04: Grading an MCQ response throws RuntimeException")
    void evaluateResponse_mcqQuestion_throwsException() {
        StudentResponse sr = StudentResponse.builder()
                .id(1L).attempt(attempt).question(mcqQuestion).build();

        EvaluateResponseRequest req = new EvaluateResponseRequest();
        req.setResponseId(1L);
        req.setMarksAwarded(3);

        when(responseRepository.findById(1L)).thenReturn(Optional.of(sr));

        assertThatThrownBy(() -> evaluationService.evaluateResponse(req, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only descriptive questions need manual evaluation");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-EVAL-05: Grade non-existent response
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EVAL-05: Grade unknown responseId throws RuntimeException")
    void evaluateResponse_unknownResponseId_throwsException() {
        EvaluateResponseRequest req = new EvaluateResponseRequest();
        req.setResponseId(999L);
        req.setMarksAwarded(5);

        when(responseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluationService.evaluateResponse(req, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Response not found");
    }

    // ─────────────────────────────────────────────────────────────
    // TC-EVAL-06: Get pending descriptive responses for an attempt
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EVAL-06: getPendingDescriptive returns unevaluated descriptive responses")
    void getPendingDescriptive_returnsCorrectList() {
        StudentResponse sr = StudentResponse.builder()
                .id(1L).attempt(attempt).question(descriptiveQuestion).build();

        when(responseRepository.findUnevaluatedDescriptive(1L)).thenReturn(List.of(sr));

        List<StudentResponse> result = evaluationService.getPendingDescriptive(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuestion().getType()).isEqualTo(QuestionType.DESCRIPTIVE);
    }

    // ─────────────────────────────────────────────────────────────
    // TC-EVAL-07: All responses evaluated — attempt finalized to EVALUATED
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-EVAL-07: After last descriptive grade, attempt status becomes EVALUATED")
    void evaluateResponse_lastPending_finalizesAttempt() {
        StudentResponse sr = StudentResponse.builder()
                .id(1L).attempt(attempt).question(descriptiveQuestion)
                .responseText("SOLID means...").build();
        attempt.getResponses().add(sr);

        EvaluateResponseRequest req = new EvaluateResponseRequest();
        req.setResponseId(1L);
        req.setMarksAwarded(7);

        when(responseRepository.findById(1L)).thenReturn(Optional.of(sr));
        when(userRepo.findById(2L)).thenReturn(Optional.of(instructor));
        when(responseRepository.save(any())).thenReturn(sr);
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(attemptRepository.save(any())).thenAnswer(inv -> {
            StudentAttempt saved = inv.getArgument(0);
            assertThat(saved.getStatus()).isEqualTo(AttemptStatus.EVALUATED);
            assertThat(saved.getTotalScore()).isEqualTo(7);
            return saved;
        });

        evaluationService.evaluateResponse(req, 2L);

        verify(attemptRepository).save(argThat(a ->
                a.getStatus() == AttemptStatus.EVALUATED && a.getTotalScore() == 7));
    }
}
