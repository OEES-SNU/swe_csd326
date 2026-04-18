package com.oees.oees.repository;

import com.oees.oees.entity.StudentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StudentResponseRepository extends JpaRepository<StudentResponse, Long> {
    List<StudentResponse> findByAttemptId(Long attemptId);

    @Query("SELECT sr FROM StudentResponse sr WHERE sr.attempt.id = :attemptId " +
            "AND sr.question.type = 'DESCRIPTIVE' AND sr.marksAwarded IS NULL")
    List<StudentResponse> findUnevaluatedDescriptive(Long attemptId);

    @Query("SELECT COUNT(sr) FROM StudentResponse sr WHERE sr.question.id = :questionId " +
            "AND sr.marksAwarded = 0 AND sr.attempt.exam.id = :examId")
    long countIncorrectResponses(Long examId, Long questionId);

    @Query("SELECT sr FROM StudentResponse sr WHERE sr.attempt.exam.id = :examId AND sr.attempt.status = 'EVALUATED'")
    List<StudentResponse> findEvaluatedResponsesByExamId(Long examId);

    @Query("SELECT sr FROM StudentResponse sr WHERE sr.attempt.exam.course.id = :courseId AND sr.attempt.status = 'EVALUATED' AND sr.marksAwarded IS NOT NULL")
    List<StudentResponse> findEvaluatedResponsesByCourseId(Long courseId);

    @Query("""
    SELECT q.id, q.content,
           SUM(CASE WHEN sr.marksAwarded = 0 THEN 1 ELSE 0 END),
           COUNT(sr)
    FROM StudentResponse sr
    JOIN sr.question q
    JOIN sr.attempt a
    WHERE a.exam.id = :examId
      AND a.status = 'EVALUATED'
    GROUP BY q.id, q.content
""")
    List<Object[]> getQuestionDifficultyData(Long examId);
}
