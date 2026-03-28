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
}
