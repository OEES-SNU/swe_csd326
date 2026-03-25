package com.oees.oees.repository;

import com.oees.oees.entity.Course;
import com.oees.oees.entity.Enrollment;
import com.oees.oees.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent(User student);
    List<Enrollment> findByCourse(Course course);
    Optional<Enrollment> findByStudentAndCourse(User student, Course course);
    boolean existsByStudentAndCourse(User student, Course course);
}