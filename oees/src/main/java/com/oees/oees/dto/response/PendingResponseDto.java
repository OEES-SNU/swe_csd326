package com.oees.oees.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingResponseDto {
    private Long responseId;
    private Long questionId;
    private String questionContent;
    private String questionType;
    private Integer maxMarks;
    private String responseText;
    private String selectedOption;
}
