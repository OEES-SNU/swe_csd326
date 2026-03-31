package com.oees.oees.service;

import com.oees.oees.dto.request.SubmitAttemptRequest;
import com.oees.oees.dto.response.AttemptResponse;
import com.oees.oees.entity.*;
import com.oees.oees.enums.AttemptStatus;
import com.oees.oees.enums.ExamStatus;
import com.oees.oees.enums.QuestionType;
import com.oees.oees.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamExecutionService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final StudentAttemptRepository attemptRepository;
    private final StudentResponseRepository responseRepository;
    private final userRepository userRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public AttemptResponse startAttempt(Long examId, Long studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (exam.getStatus() != ExamStatus.ACTIVE) {
            throw new RuntimeException("Exam is not currently active");
        }

        int attemptCount = attemptRepository.countByExamIdAndStudentId(examId, studentId);
        if (attemptCount >= exam.getMaxAttempts()) {
            throw new RuntimeException("Maximum attempt limit reached");
        }

        attemptRepository.findByExamIdAndStudentIdAndStatus(examId, studentId, AttemptStatus.IN_PROGRESS)
                .ifPresent(a -> {
                    throw new RuntimeException("Active attempt already exists");
                });

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StudentAttempt attempt = StudentAttempt.builder()
                .exam(exam)
                .student(student)
                .startedAt(LocalDateTime.now())
                .attemptNumber(attemptCount + 1)
                .status(AttemptStatus.IN_PROGRESS)
                .build();
        attempt = attemptRepository.save(attempt);

        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamIdOrderByOrderIndex(examId);
        LocalDateTime deadline = attempt.getStartedAt().plusMinutes(exam.getDurationMinutes());

        List<AttemptResponse.QuestionItem> items = examQuestions.stream()
                .map(eq -> {
                    Question q = eq.getQuestion();
                    List<String> options = null;
                    if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
                        options = new ArrayList<>();
                        if (q.getOptionA() != null)
                            options.add(q.getOptionA());
                        if (q.getOptionB() != null)
                            options.add(q.getOptionB());
                        if (q.getOptionC() != null)
                            options.add(q.getOptionC());
                        if (q.getOptionD() != null)
                            options.add(q.getOptionD());
                    }
                    return AttemptResponse.QuestionItem.builder()
                            .questionId(q.getId())
                            .content(q.getContent())
                            .type(q.getType().name())
                            .options(options)
                            .marks(eq.getMarks())
                            .orderIndex(eq.getOrderIndex())
                            .build();
                })
                .collect(Collectors.toList());

        return AttemptResponse.builder()
                .attemptId(attempt.getId())
                .startedAt(attempt.getStartedAt())
                .deadline(deadline)
                .questions(items)
                .build();
    }

    public record SubmitResult(Integer score, Integer totalMarks, boolean pendingEvaluation) {}

    @Transactional
    public SubmitResult submitAttempt(Long attemptId, Long studentId, SubmitAttemptRequest req) {
        StudentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (!attempt.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized");
        }
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Attempt already submitted");
        }

        List<StudentResponse> responses = new ArrayList<>();
        for (SubmitAttemptRequest.ResponseItem item : req.getResponses()) {
            Question q = questionRepository.findById(item.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            StudentResponse sr = StudentResponse.builder()
                    .attempt(attempt)
                    .question(q)
                    .selectedOption(item.getSelectedOption())
                    .responseText(item.getResponseText())
                    .build();
            responses.add(sr);
        }
        responseRepository.saveAll(responses);

        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attemptRepository.save(attempt);

        autoEvaluateObjective(attempt.getId());

        // Determine result to return to student
        List<StudentResponse> saved = responseRepository.findByAttemptId(attempt.getId());
        boolean hasPending = saved.stream().anyMatch(r -> r.getMarksAwarded() == null);
        int autoScore = saved.stream()
                .filter(r -> r.getMarksAwarded() != null)
                .mapToInt(StudentResponse::getMarksAwarded)
                .sum();
        int totalMarks = attempt.getExam().getTotalMarks();

        return new SubmitResult(autoScore, totalMarks, hasPending);
    }

    @Transactional
    public void autoEvaluateObjective(Long attemptId) {
        List<StudentResponse> responses = responseRepository.findByAttemptId(attemptId);
        for (StudentResponse sr : responses) {
            Question q = sr.getQuestion();
            if (q.getType() == QuestionType.MULTIPLE_CHOICE || q.getType() == QuestionType.FILL_IN_THE_BLANK) {
                String correct = q.getCorrectAnswer() != null ? q.getCorrectAnswer().trim() : null;
                String given = q.getType() == QuestionType.MULTIPLE_CHOICE
                        ? sr.getSelectedOption()
                        : sr.getResponseText();
                String givenTrimmed = given != null ? given.trim() : null;
                sr.setMarksAwarded(correct != null && correct.equalsIgnoreCase(givenTrimmed)
                        ? (q.getMarks() != null ? q.getMarks() : 0)
                        : 0);
                sr.setEvaluatedAt(LocalDateTime.now());
            }
        }
        responseRepository.saveAll(responses);

        // If no descriptive questions remain unevaluated, finalize the attempt
        boolean hasUnevaluatedDescriptive = responses.stream()
                .anyMatch(sr -> sr.getQuestion().getType() == QuestionType.DESCRIPTIVE
                        && sr.getMarksAwarded() == null);
        if (!hasUnevaluatedDescriptive) {
            StudentAttempt attempt = attemptRepository.findById(attemptId)
                    .orElseThrow(() -> new RuntimeException("Attempt not found"));
            int total = responses.stream()
                    .filter(sr -> sr.getMarksAwarded() != null)
                    .mapToInt(StudentResponse::getMarksAwarded)
                    .sum();
            attempt.setTotalScore(total);
            attempt.setStatus(AttemptStatus.EVALUATED);
            attemptRepository.save(attempt);
        }
    }
}
