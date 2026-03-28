package com.oees.oees.controller;

import com.oees.oees.dto.response.ResultResponse;
import com.oees.oees.repository.userRepository;
import com.oees.oees.security.JwtUtil;
import com.oees.oees.service.AnalyticsService;
import com.oees.oees.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;
    private final AnalyticsService analyticsService;
    private final JwtUtil jwtUtil;
    private final userRepository userRepository;

    @PostMapping("/generate/{examId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<String> generateResults(@PathVariable Long examId) {
        resultService.generateResults(examId);
        return ResponseEntity.ok("Results generated successfully");
    }

    @GetMapping("/exam/{examId}/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResultResponse> getMyResult(
            @PathVariable Long examId,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Long studentId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(resultService.getStudentResult(examId, studentId));
    }

    @GetMapping("/analytics/exam/{examId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<AnalyticsService.ExamAnalytics> getExamAnalytics(@PathVariable Long examId) {
        return ResponseEntity.ok(analyticsService.getExamAnalytics(examId));
    }

    @GetMapping("/analytics/course/{courseId}/average")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<Double> getCourseAverage(@PathVariable Long courseId) {
        return ResponseEntity.ok(analyticsService.getCourseAverage(courseId));
    }
}
