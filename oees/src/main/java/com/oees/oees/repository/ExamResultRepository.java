package com.oees.oees.repository;

import com.oees.oees.entity.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {
    Optional<ExamResult> findByAttemptId(Long attemptId);

    List<ExamResult> findByExamIdOrderByTotalMarksDesc(Long examId);

    List<ExamResult> findByStudentId(Long studentId);

    @Query("SELECT AVG(r.totalMarks) FROM ExamResult r WHERE r.exam.id = :examId")
    Double findAverageMarksByExamId(Long examId);

    @Query("SELECT AVG(r.totalMarks) FROM ExamResult r WHERE r.exam.course.id = :courseId")
    Double findAverageMarksByCourseId(Long courseId);
}
