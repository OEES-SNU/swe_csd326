package com.oees.oees.controller;

import com.oees.oees.dto.request.EvaluateResponseRequest;
import com.oees.oees.entity.StudentResponse;
import com.oees.oees.repository.userRepository;
import com.oees.oees.security.JwtUtil;
import com.oees.oees.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final JwtUtil jwtUtil;
    private final userRepository userRepository;

    @GetMapping("/attempt/{attemptId}/pending")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<StudentResponse>> getPendingDescriptive(@PathVariable Long attemptId) {
        return ResponseEntity.ok(evaluationService.getPendingDescriptive(attemptId));
    }

    @PostMapping("/grade")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<String> gradeResponse(
            @RequestBody EvaluateResponseRequest req,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Long instructorId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        evaluationService.evaluateResponse(req, instructorId);
        return ResponseEntity.ok("Response graded successfully");
    }
}
