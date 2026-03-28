package com.oees.oees.service;

import com.oees.oees.dto.request.ExamRequest;
import com.oees.oees.dto.response.ExamResponse;
import com.oees.oees.entity.*;
import com.oees.oees.enums.ExamStatus;
import com.oees.oees.enums.QuestionType;
import com.oees.oees.enums.DifficultyLevel;
import com.oees.oees.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final userRepository userRepository;

    @Transactional
    public ExamResponse createExam(ExamRequest req, Long instructorId) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exam exam = Exam.builder()
                .title(req.getTitle())
                .course(course)
                .instructor(instructor)
                .durationMinutes(req.getDurationMinutes())
                .totalMarks(req.getTotalMarks())
                .maxAttempts(req.getMaxAttempts() != null ? req.getMaxAttempts() : 1)
                .passMark(req.getPassMark())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .status(req.getStartTime() != null ? ExamStatus.SCHEDULED : ExamStatus.DRAFT)
                .build();

        exam = examRepository.save(exam);

        List<Long> questionIds = resolveQuestions(req);
        List<ExamQuestion> examQuestions = new ArrayList<>();
        for (int i = 0; i < questionIds.size(); i++) {
            Question q = questionRepository.findById(questionIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            examQuestions.add(ExamQuestion.builder()
                    .exam(exam)
                    .question(q)
                    .marks(q.getMarks())
                    .orderIndex(i + 1)
                    .build());
        }
        examQuestionRepository.saveAll(examQuestions);

        return toResponse(exam, examQuestions.size());
    }

    private List<Long> resolveQuestions(ExamRequest req) {
        if (req.getAutoSelect() != null) {
            ExamRequest.AutoSelectCriteria c = req.getAutoSelect();
            return questionRepository.findAll().stream()
                    .filter(q -> c.getUnit() == null || c.getUnit().equals(q.getUnit()))
                    .filter(q -> c.getDifficultyLevel() == null ||
                            DifficultyLevel.valueOf(c.getDifficultyLevel()) == q.getDifficultyLevel())
                    .filter(q -> c.getQuestionType() == null ||
                            QuestionType.valueOf(c.getQuestionType()) == q.getType())
                    .limit(c.getCount() != null ? c.getCount() : 10)
                    .map(Question::getId)
                    .collect(Collectors.toList());
        }
        return req.getQuestionIds();
    }

    public List<ExamResponse> getExamsForStudent(Long studentId) {
        return examRepository.findAvailableExamsForStudent(studentId).stream()
                .map(e -> toResponse(e, e.getExamQuestions() != null ? e.getExamQuestions().size() : 0))
                .collect(Collectors.toList());
    }

    public List<ExamResponse> getExamsByCourse(Long courseId) {
        return examRepository.findByCourseIdAndStatus(courseId, ExamStatus.ACTIVE).stream()
                .map(e -> toResponse(e, e.getExamQuestions() != null ? e.getExamQuestions().size() : 0))
                .collect(Collectors.toList());
    }

    // Called by scheduler to update exam statuses
    @Transactional
    public void updateExamStatuses() {
        LocalDateTime now = LocalDateTime.now();
        examRepository.findExamsToActivate(now).forEach(e -> {
            e.setStatus(ExamStatus.ACTIVE);
            examRepository.save(e);
        });
        examRepository.findExamsToExpire(now).forEach(e -> {
            e.setStatus(ExamStatus.EXPIRED);
            examRepository.save(e);
        });
    }

    private ExamResponse toResponse(Exam e, int qCount) {
        return ExamResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .courseName(e.getCourse().getCourseName())
                .durationMinutes(e.getDurationMinutes())
                .totalMarks(e.getTotalMarks())
                .maxAttempts(e.getMaxAttempts())
                .passMark(e.getPassMark())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .status(e.getStatus())
                .questionCount(qCount)
                .build();
    }
}
