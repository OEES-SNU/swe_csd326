package com.oees.oees.repository;

import com.oees.oees.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    List<ExamQuestion> findByExamIdOrderByOrderIndex(Long examId);

    boolean existsByQuestionId(Long questionId);

    @Query("SELECT CASE WHEN COUNT(eq) > 0 THEN true ELSE false END FROM ExamQuestion eq " +
           "WHERE eq.question.id = :questionId " +
           "AND eq.exam.status IN (com.oees.oees.enums.ExamStatus.SCHEDULED, com.oees.oees.enums.ExamStatus.ACTIVE)")
    boolean isQuestionInActiveOrScheduledExam(@Param("questionId") Long questionId);
}
