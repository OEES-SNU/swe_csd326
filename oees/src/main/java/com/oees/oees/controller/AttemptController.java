package com.oees.oees.controller;

import com.oees.oees.dto.request.SubmitAttemptRequest;
import com.oees.oees.dto.response.AttemptResponse;
import com.oees.oees.repository.userRepository;
import com.oees.oees.security.JwtUtil;
import com.oees.oees.service.ExamExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams/{examId}/attempt")
@RequiredArgsConstructor
public class AttemptController {

    private final ExamExecutionService executionService;
    private final JwtUtil jwtUtil;
    private final userRepository userRepository;

    @PostMapping("/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AttemptResponse> startAttempt(
            @PathVariable Long examId,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Long studentId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(executionService.startAttempt(examId, studentId));
    }

    @PostMapping("/{attemptId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExamExecutionService.SubmitResult> submitAttempt(
            @PathVariable Long examId,
            @PathVariable Long attemptId,
            @RequestBody SubmitAttemptRequest req,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Long studentId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        ExamExecutionService.SubmitResult result = executionService.submitAttempt(attemptId, studentId, req);
        return ResponseEntity.ok(result);
    }
}
