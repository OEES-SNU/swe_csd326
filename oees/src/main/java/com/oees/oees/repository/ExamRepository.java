package com.oees.oees.repository;

import com.oees.oees.entity.Exam;
import com.oees.oees.enums.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCourseIdAndStatus(Long courseId, ExamStatus status);

    @Query("SELECT e FROM Exam e WHERE e.course.id IN " +
            "(SELECT en.course.id FROM Enrollment en WHERE en.student.id = :studentId) " +
            "AND e.status IN ('SCHEDULED', 'ACTIVE')")
    List<Exam> findAvailableExamsForStudent(Long studentId);

    @Query("SELECT e FROM Exam e WHERE e.status = 'SCHEDULED' AND e.startTime <= :now")
    List<Exam> findExamsToActivate(LocalDateTime now);

    @Query("SELECT e FROM Exam e WHERE e.status = 'ACTIVE' AND e.endTime <= :now")
    List<Exam> findExamsToExpire(LocalDateTime now);
}
