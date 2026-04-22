package com.oees.oees.repository;

import com.oees.oees.entity.Exam;
import com.oees.oees.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCourseIdAndStatus(Long courseId, ExamStatus status);
    List<Exam> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @Query("SELECT e FROM Exam e WHERE e.course.id IN " +
            "(SELECT en.course.id FROM Enrollment en WHERE en.student.id = :studentId) " +
            "AND e.status IN (com.oees.oees.enums.ExamStatus.SCHEDULED, com.oees.oees.enums.ExamStatus.ACTIVE)")
    List<Exam> findAvailableExamsForStudent(@Param("studentId") Long studentId);

    @Modifying
    @Query(value = "UPDATE exams SET status = 'ACTIVE' WHERE status = 'SCHEDULED' AND start_time <= NOW() + INTERVAL '5 hours 30 minutes'", nativeQuery = true)
    void activateExams();

    @Modifying
    @Query(value = "UPDATE exams SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND end_time <= NOW() + INTERVAL '5 hours 30 minutes'", nativeQuery = true)
    void expireExams();
}
