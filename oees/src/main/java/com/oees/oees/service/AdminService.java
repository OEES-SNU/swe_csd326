package com.oees.oees.service;

import com.oees.oees.dto.request.CourseRequest;
import com.oees.oees.dto.request.EnrollmentRequest;
import com.oees.oees.entity.Course;
import com.oees.oees.entity.Enrollment;
import com.oees.oees.entity.User;
import com.oees.oees.enums.Role;
import com.oees.oees.repository.CourseRepository;
import com.oees.oees.repository.EnrollmentRepository;
import com.oees.oees.repository.userRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final userRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public Course createCourse(CourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new RuntimeException("Course code already exists");
        }
        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .active(true)
                .build();
        return courseRepository.save(course);
    }

    public Course assignInstructor(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));
        if (instructor.getRole() != Role.INSTRUCTOR) {
            throw new RuntimeException("User is not an instructor");
        }
        course.setInstructor(instructor);
        return courseRepository.save(course);
    }

    public Enrollment enrollStudent(EnrollmentRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException("User is not a student");
        }
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Student already enrolled");
        }
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(Enrollment.EnrollmentStatus.APPROVED)
                .build();
        return enrollmentRepository.save(enrollment);
    }

    public List<User> getAllStudents() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .toList();
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getUnassignedCourses() {
        return courseRepository.findByInstructorIsNull();
    }
}