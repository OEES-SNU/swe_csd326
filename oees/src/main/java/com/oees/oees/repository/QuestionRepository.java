package com.oees.oees.repository;

import com.oees.oees.entity.Course;
import com.oees.oees.entity.Question;
import com.oees.oees.enums.DifficultyLevel;
import com.oees.oees.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCourse(Course course);
    List<Question> findByCourseAndType(Course course, QuestionType type);
    List<Question> findByCourseAndDifficultyLevel(Course course, DifficultyLevel level);
    List<Question> findByCourseAndTypeAndDifficultyLevel(Course course, QuestionType type, DifficultyLevel level);
}