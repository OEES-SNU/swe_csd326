package com.oees.oees.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamRequest {
    private String title;
    private Long courseId;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer maxAttempts;
    private Integer passMark;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Long> questionIds; // manual selection
    private AutoSelectCriteria autoSelect; // automated selection (null = manual)

    @Data
    public static class AutoSelectCriteria {
        private String unit;
        private String difficultyLevel;
        private String questionType;
        private Integer count;
    }
}
