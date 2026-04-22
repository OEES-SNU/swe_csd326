package com.oees.oees.service;

import com.oees.oees.entity.Exam;
import com.oees.oees.entity.ExamResult;
import com.oees.oees.entity.StudentAttempt;
import com.oees.oees.entity.StudentResponse;
import com.oees.oees.enums.AttemptStatus;
import com.oees.oees.repository.ExamRepository;
import com.oees.oees.repository.ExamResultRepository;
import com.oees.oees.repository.StudentAttemptRepository;
import com.oees.oees.repository.StudentResponseRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final StudentAttemptRepository attemptRepository;
    private final ExamRepository examRepository;
    private final StudentResponseRepository responseRepository;
    private final ExamResultRepository resultRepository;

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

    @Data
    @Builder
    public static class UnitAnalytics {
        private String unit;
        private Double averagePercentage;
    }

    @Transactional(readOnly = true)
    public List<UnitAnalytics> getExamUnitAnalytics(Long examId) {

        List<StudentResponse> responses = responseRepository
                .findEvaluatedResponsesByExamId(examId);

        // Group by unit
        Map<String, List<StudentResponse>> grouped = responses.stream()
                .collect(Collectors.groupingBy(r -> r.getQuestion().getUnit()));

        List<UnitAnalytics> result = new ArrayList<>();

        for (Map.Entry<String, List<StudentResponse>> entry : grouped.entrySet()) {

            String unit = entry.getKey();
            List<StudentResponse> unitResponses = entry.getValue();

            int totalObtained = unitResponses.stream()
                    .mapToInt(r -> r.getMarksAwarded() != null ? r.getMarksAwarded() : 0)
                    .sum();

            int totalPossible = unitResponses.stream()
                    .mapToInt(r -> r.getQuestion().getMarks())
                    .sum();

            double percentage = totalPossible == 0
                    ? 0
                    : (totalObtained * 100.0) / totalPossible;

            result.add(UnitAnalytics.builder()
                    .unit(unit)
                    .averagePercentage(percentage)
                    .build());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<UnitAnalytics> getCourseUnitAnalytics(Long courseId) {

        List<StudentResponse> responses = responseRepository
                .findEvaluatedResponsesByCourseId(courseId);

        Map<String, List<StudentResponse>> grouped = responses.stream()
                .collect(Collectors.groupingBy(r -> r.getQuestion().getUnit()));

        List<UnitAnalytics> result = new ArrayList<>();

        for (Map.Entry<String, List<StudentResponse>> entry : grouped.entrySet()) {

            String unit = entry.getKey();
            List<StudentResponse> unitResponses = entry.getValue();

            int totalObtained = unitResponses.stream()
                    .mapToInt(StudentResponse::getMarksAwarded)
                    .sum();

            int totalPossible = unitResponses.stream()
                    .mapToInt(r -> r.getQuestion().getMarks())
                    .sum();

            double percentage = totalPossible == 0
                    ? 0
                    : (totalObtained * 100.0) / totalPossible;

            result.add(UnitAnalytics.builder()
                    .unit(unit)
                    .averagePercentage(percentage)
                    .build());
        }

        return result;
    }

    @Data
    @Builder
    public static class QuestionDifficulty {
        private Long questionId;
        private String content;
        private Double difficultyPercentage;
    }

    @Transactional(readOnly = true)
    public List<QuestionDifficulty> getQuestionDifficulty(Long examId) {

        List<Object[]> data = responseRepository.getQuestionDifficultyData(examId);

        return data.stream().map(row -> {
            Long questionId = (Long) row[0];
            String content = (String) row[1];
            Long incorrect = (Long) row[2];
            Long total = (Long) row[3];

            double difficulty = total == 0 ? 0 : (incorrect * 100.0) / total;

            return QuestionDifficulty.builder()
                    .questionId(questionId)
                    .content(content)
                    .difficultyPercentage(difficulty)
                    .build();
        }).toList();
    }

    @Data
    @Builder
    public static class TopPerformer {
        private Integer rank;
        private String studentName;
        private Integer score;
        private Integer maxMarks;
        private String grade;
    }

    @Transactional(readOnly = true)
    public List<TopPerformer> getTopPerformers(Long examId) {
        return resultRepository.findByExamIdOrderByTotalMarksDesc(examId).stream()
                .limit(10)
                .map(r -> TopPerformer.builder()
                        .rank(r.getRank())
                        .studentName(r.getStudent().getName())
                        .score(r.getTotalMarks())
                        .maxMarks(r.getExam().getTotalMarks())
                        .grade(r.getGrade())
                        .build())
                .collect(Collectors.toList());
    }
}
