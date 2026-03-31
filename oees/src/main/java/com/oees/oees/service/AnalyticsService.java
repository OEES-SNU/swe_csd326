package com.oees.oees.service;

import com.oees.oees.entity.Exam;
import com.oees.oees.entity.StudentAttempt;
import com.oees.oees.enums.AttemptStatus;
import com.oees.oees.repository.ExamRepository;
import com.oees.oees.repository.StudentAttemptRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final StudentAttemptRepository attemptRepository;
    private final ExamRepository examRepository;

    @Transactional(readOnly = true)
    public ExamAnalytics getExamAnalytics(Long examId) {
        Exam exam = examRepository.findById(examId).orElse(null);
        Integer passMark = exam != null ? exam.getPassMark() : null;

        List<StudentAttempt> evaluated = attemptRepository.findByExamId(examId).stream()
                .filter(a -> a.getStatus() == AttemptStatus.EVALUATED && a.getTotalScore() != null)
                .toList();

        if (evaluated.isEmpty()) {
            return ExamAnalytics.builder()
                    .examId(examId)
                    .totalAttempts(0)
                    .build();
        }

        OptionalDouble avg = evaluated.stream().mapToInt(StudentAttempt::getTotalScore).average();
        OptionalInt max = evaluated.stream().mapToInt(StudentAttempt::getTotalScore).max();
        OptionalInt min = evaluated.stream().mapToInt(StudentAttempt::getTotalScore).min();

        Double passRate = null;
        if (passMark != null) {
            long passed = evaluated.stream().filter(a -> a.getTotalScore() >= passMark).count();
            passRate = (passed * 100.0) / evaluated.size();
        }

        return ExamAnalytics.builder()
                .examId(examId)
                .averageScore(avg.isPresent() ? avg.getAsDouble() : null)
                .passRate(passRate)
                .highestScore(max.isPresent() ? max.getAsInt() : null)
                .lowestScore(min.isPresent() ? min.getAsInt() : null)
                .totalAttempts(evaluated.size())
                .build();
    }

    @Transactional(readOnly = true)
    public Double getCourseAverage(Long courseId) {
        OptionalDouble avg = attemptRepository.findAll().stream()
                .filter(a -> a.getStatus() == AttemptStatus.EVALUATED
                        && a.getTotalScore() != null
                        && a.getExam().getCourse().getId().equals(courseId))
                .mapToInt(StudentAttempt::getTotalScore)
                .average();
        return avg.isPresent() ? avg.getAsDouble() : null;
    }

    @Data
    @Builder
    public static class ExamAnalytics {
        private Long examId;
        private Double averageScore;
        private Double passRate;
        private Integer highestScore;
        private Integer lowestScore;
        private int totalAttempts;
    }
}
