package com.oees.oees.dto.request;

import com.oees.oees.enums.DifficultyLevel;
import com.oees.oees.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionRequest {

    @NotBlank(message = "Question content is required")
    private String content;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;

    @NotNull(message = "Marks is required")
    private Integer marks;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
}