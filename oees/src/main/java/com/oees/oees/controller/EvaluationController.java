package com.oees.oees.controller;

import com.oees.oees.dto.request.EvaluateResponseRequest;
import com.oees.oees.entity.StudentAttempt;
import com.oees.oees.entity.StudentResponse;
import com.oees.oees.enums.AttemptStatus;
import com.oees.oees.repository.StudentAttemptRepository;
import com.oees.oees.repository.userRepository;
import com.oees.oees.security.JwtUtil;
import com.oees.oees.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final JwtUtil jwtUtil;
    private final userRepository userRepository;
    private final StudentAttemptRepository studentAttemptRepository;

    @GetMapping("/exam/{examId}/attempts")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAttemptsByExam(@PathVariable Long examId) {
        List<Map<String, Object>> result = studentAttemptRepository.findByExamId(examId).stream()
            .filter(a -> a.getStatus() != AttemptStatus.IN_PROGRESS)
            .map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", a.getId());
                map.put("attemptNumber", a.getAttemptNumber());
                map.put("status", a.getStatus());
                map.put("submittedAt", a.getSubmittedAt());
                map.put("totalScore", a.getTotalScore());
                map.put("studentName", a.getStudent() != null ? a.getStudent().getName() : null);
                map.put("studentEmail", a.getStudent() != null ? a.getStudent().getEmail() : null);
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

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
