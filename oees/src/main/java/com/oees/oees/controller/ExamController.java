package com.oees.oees.controller;

import com.oees.oees.dto.request.ExamRequest;
import com.oees.oees.dto.response.ExamResponse;
import com.oees.oees.entity.Course;
import com.oees.oees.entity.Enrollment;
import com.oees.oees.entity.User;
import com.oees.oees.repository.EnrollmentRepository;
import com.oees.oees.repository.userRepository;
import com.oees.oees.security.JwtUtil;
import com.oees.oees.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final JwtUtil jwtUtil;
    private final userRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ExamResponse> createExam(
            @RequestBody ExamRequest req,
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Long instructorId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(examService.createExam(req, instructorId));
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ExamResponse>> getAvailableExams(
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        Long studentId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
        return ResponseEntity.ok(examService.getExamsForStudent(studentId));
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Course>> getMyCourses(
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.extractUsername(token.substring(7));
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Course> courses = enrollmentRepository.findByStudent(student)
                .stream()
                .map(Enrollment::getCourse)
                .toList();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<List<ExamResponse>> getExamsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(examService.getExamsByCourse(courseId));
    }
}
