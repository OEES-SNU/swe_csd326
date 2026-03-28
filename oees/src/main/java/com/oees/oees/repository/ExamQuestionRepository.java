package com.oees.oees.repository;

import com.oees.oees.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    List<ExamQuestion> findByExamIdOrderByOrderIndex(Long examId);

    boolean existsByQuestionId(Long questionId);
}
