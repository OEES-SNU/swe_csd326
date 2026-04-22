package com.oees.oees.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private List<Long> questionIds;
    private AutoSelectCriteria autoSelect;
    private Map<String, Integer> gradingScale;

    @Data
    public static class AutoSelectCriteria {
        private String unit;
        private String difficultyLevel;
        private String questionType;
        private Integer count;
    }
}
