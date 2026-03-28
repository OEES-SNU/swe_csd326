package com.oees.oees.dto.response;

import com.oees.oees.enums.ExamStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ExamResponse {
    private Long id;
    private String title;
    private String courseName;
    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer maxAttempts;
    private Integer passMark;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ExamStatus status;
    private int questionCount;
}
