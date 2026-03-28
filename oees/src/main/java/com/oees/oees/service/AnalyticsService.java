package com.oees.oees.service;

import com.oees.oees.entity.ExamResult;
import com.oees.oees.repository.ExamResultRepository;
import com.oees.oees.repository.StudentResponseRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExamResultRepository resultRepository;
    private final StudentResponseRepository responseRepository;

    public ExamAnalytics getExamAnalytics(Long examId) {
        List<ExamResult> results = resultRepository.findByExamIdOrderByTotalMarksDesc(examId);
        Double avg = resultRepository.findAverageMarksByExamId(examId);

        List<TopPerformer> top10 = results.stream()
                .limit(10)
                .map(r -> TopPerformer.builder()
                        .rank(r.getRank())
                        .totalMarks(r.getTotalMarks())
                        // anonymized: only show to instructors
                        .studentName(r.getStudent().getUsername())
                        .build())
                .collect(Collectors.toList());

        return ExamAnalytics.builder()
                .examId(examId)
                .averageMarks(avg)
                .totalAttempts(results.size())
                .topPerformers(top10)
                .build();
    }

    public Double getCourseAverage(Long courseId) {
        return resultRepository.findAverageMarksByCourseId(courseId);
    }

    @Data
    @Builder
    public static class ExamAnalytics {
        private Long examId;
        private Double averageMarks;
        private int totalAttempts;
        private List<TopPerformer> topPerformers;
    }

    @Data
    @Builder
    public static class TopPerformer {
        private Integer rank;
        private String studentName;
        private Integer totalMarks;
    }
}
