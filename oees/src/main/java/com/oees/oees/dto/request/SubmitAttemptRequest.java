package com.oees.oees.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class SubmitAttemptRequest {
    private List<ResponseItem> responses;

    @Data
    public static class ResponseItem {
        private Long questionId;
        private String selectedOption; // MCQ
        private String responseText; // descriptive / fill-in-blank
    }
}
