package com.oees.oees.service;

import com.oees.oees.dto.request.QuestionRequest;
import com.oees.oees.entity.Course;
import com.oees.oees.entity.Question;
import com.oees.oees.entity.User;
import com.oees.oees.repository.CourseRepository;
import com.oees.oees.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;

    public Question createQuestion(QuestionRequest request, User instructor) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Question question = Question.builder()
                .content(request.getContent())
                .type(request.getType())
                .difficultyLevel(request.getDifficultyLevel())
                .marks(request.getMarks())
                .unit(request.getUnit())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .correctAnswer(request.getCorrectAnswer())
                .course(course)
                .createdBy(instructor)
                .active(true)
                .build();

        return questionRepository.save(question);
    }

    public List<Question> getQuestionsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return questionRepository.findByCourse(course);
    }

    public Question updateQuestion(Long questionId,
                                   QuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setContent(request.getContent());
        question.setType(request.getType());
        question.setDifficultyLevel(request.getDifficultyLevel());
        question.setMarks(request.getMarks());
        question.setUnit(request.getUnit());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectAnswer(request.getCorrectAnswer());
        return questionRepository.save(question);
    }

    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        questionRepository.delete(question);
    }
}