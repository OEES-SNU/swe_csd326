package com.oees.oees.service;

import com.oees.oees.dto.response.ResultResponse;
import com.oees.oees.entity.*;
import com.oees.oees.enums.AttemptStatus;
import com.oees.oees.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final StudentAttemptRepository attemptRepository;
    private final StudentResponseRepository responseRepository;
    private final ExamResultRepository resultRepository;
    private final ExamRepository examRepository;

    @Transactional
    public void generateResults(Long examId) {

        List<StudentAttempt> attempts = new java.util.ArrayList<>();
        attempts.addAll(attemptRepository.findByExamIdAndStatus(examId, AttemptStatus.EVALUATED));
        attempts.addAll(attemptRepository.findByExamIdAndStatus(examId, AttemptStatus.SUBMITTED));

        boolean anyPending = attempts.stream()
                .anyMatch(a -> a.getStatus() == AttemptStatus.SUBMITTED);

        if (anyPending) {
            throw new RuntimeException("Not all attempts are evaluated yet");
        }

        List<StudentAttempt> evaluated = attempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.EVALUATED)
                .toList();


//        List<StudentAttempt> evaluated = attemptRepository
//                .findByExamIdAndStatus(examId, AttemptStatus.EVALUATED);

//        List<StudentAttempt> evaluated = new java.util.ArrayList<>();
//        evaluated.addAll(attemptRepository.findByExamIdAndStatus(examId, AttemptStatus.EVALUATED));
//        evaluated.addAll(attemptRepository.findByExamIdAndStatus(examId, AttemptStatus.SUBMITTED));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        // Compute total marks for each attempt
        Map<Long, Integer> totals = evaluated.stream().collect(Collectors.toMap(
                StudentAttempt::getId,
                attempt -> responseRepository.findByAttemptId(attempt.getId()).stream()
                        .mapToInt(r -> r.getMarksAwarded() != null ? r.getMarksAwarded() : 0)
                        .sum()));

        // Sort to assign ranks
        List<Map.Entry<Long, Integer>> sorted = totals.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        AtomicInteger rank = new AtomicInteger(1);
        for (Map.Entry<Long, Integer> entry : sorted) {
            StudentAttempt attempt = evaluated.stream()
                    .filter(a -> a.getId().equals(entry.getKey()))
                    .findFirst().orElseThrow();

            int total = entry.getValue();
            String grade = computeGrade(total, exam.getTotalMarks());
            boolean passed = exam.getPassMark() != null && total >= exam.getPassMark();

            ExamResult result = resultRepository.findByAttemptId(attempt.getId())
                    .orElse(ExamResult.builder()
                            .attempt(attempt)
                            .student(attempt.getStudent())
                            .exam(exam)
                            .build());

            result.setTotalMarks(total);
            result.setGrade(grade);
            result.setPassed(passed);
            result.setRank(rank.getAndIncrement());
            result.setGeneratedAt(LocalDateTime.now());
            resultRepository.save(result);
        }
    }

    public ResultResponse getStudentResult(Long examId, Long studentId) {
        List<StudentAttempt> attempts = attemptRepository.findByStudentId(studentId);
        StudentAttempt attempt = attempts.stream()
                .filter(a -> a.getExam().getId().equals(examId) &&
                        (a.getStatus() == AttemptStatus.EVALUATED || a.getStatus() == AttemptStatus.SUBMITTED))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Result not available yet"));

        ExamResult result = resultRepository.findByAttemptId(attempt.getId())
                .orElseThrow(() -> new RuntimeException("Result not generated yet"));

        Double avg = resultRepository.findAverageMarksByExamId(examId);

        return ResultResponse.builder()
                .examId(examId)
                .examTitle(attempt.getExam().getTitle())
                .totalMarks(result.getTotalMarks())
                .maxMarks(attempt.getExam().getTotalMarks())
                .grade(result.getGrade())
                .passed(result.getPassed())
                .rank(result.getRank())
                .examAverage(avg)
                .build();
    }

    private String computeGrade(int marks, int max) {
        double pct = (double) marks / max * 100;
        if (pct >= 90)
            return "A+";
        if (pct >= 80)
            return "A";
        if (pct >= 70)
            return "B";
        if (pct >= 60)
            return "C";
        if (pct >= 50)
            return "D";
        return "F";
    }
}
