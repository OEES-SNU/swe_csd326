package com.oees.oees.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AttemptResponse {
    private Long attemptId;
    private LocalDateTime startedAt;
    private LocalDateTime deadline;
    private List<QuestionItem> questions;

    @Data
    @Builder
    public static class QuestionItem {
        private Long questionId;
        private String content;
        private String type;
        private List<String> options; // for MCQ
        private Integer marks;
        private Integer orderIndex;
    }
}
