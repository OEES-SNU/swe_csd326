package com.oees.oees.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseRequest {

    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotBlank(message = "Course name is required")
    private String courseName;

    private String description;
}