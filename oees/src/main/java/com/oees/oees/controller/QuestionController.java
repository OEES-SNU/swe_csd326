package com.oees.oees.controller;

import com.oees.oees.dto.request.QuestionRequest;
import com.oees.oees.entity.Question;
import com.oees.oees.entity.User;
import com.oees.oees.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<Question> createQuestion(
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal User instructor) {
        return ResponseEntity.ok(
                questionService.createQuestion(request, instructor));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Question>> getQuestionsByCourse(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(
                questionService.getQuestionsByCourse(courseId));
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(
                questionService.updateQuestion(questionId, request));
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
}