package com.oees.oees.repository;

import com.oees.oees.entity.Course;
import com.oees.oees.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    boolean existsByCourseCode(String courseCode);
    List<Course> findByInstructor(User instructor);
    List<Course> findByInstructorIsNull();
}