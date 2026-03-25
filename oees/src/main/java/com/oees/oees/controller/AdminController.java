package com.oees.oees.controller;

import com.oees.oees.dto.request.CourseRequest;
import com.oees.oees.dto.request.EnrollmentRequest;
import com.oees.oees.entity.Course;
import com.oees.oees.entity.Enrollment;
import com.oees.oees.entity.User;
import com.oees.oees.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(adminService.createCourse(request));
    }

    @PutMapping("/courses/{courseId}/assign-instructor/{instructorId}")
    public ResponseEntity<Course> assignInstructor(
            @PathVariable Long courseId,
            @PathVariable Long instructorId) {
        return ResponseEntity.ok(
                adminService.assignInstructor(courseId, instructorId));
    }

    @PostMapping("/enroll")
    public ResponseEntity<Enrollment> enrollStudent(
            @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(adminService.enrollStudent(request));
    }

    @GetMapping("/students")
    public ResponseEntity<List<User>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(adminService.getAllCourses());
    }

    @GetMapping("/courses/unassigned")
    public ResponseEntity<List<Course>> getUnassignedCourses() {
        return ResponseEntity.ok(adminService.getUnassignedCourses());
    }
}