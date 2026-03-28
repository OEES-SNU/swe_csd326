package com.oees.oees.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultResponse {
    private Long examId;
    private String examTitle;
    private Integer totalMarks;
    private Integer maxMarks;
    private String grade;
    private Boolean passed;
    private Integer rank;
    private Double examAverage;
}
