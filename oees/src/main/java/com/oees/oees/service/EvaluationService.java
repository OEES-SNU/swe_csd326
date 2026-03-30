package com.oees.oees.service;

import com.oees.oees.dto.request.EvaluateResponseRequest;
import com.oees.oees.entity.StudentAttempt;
import com.oees.oees.entity.StudentResponse;
import com.oees.oees.entity.User;
import com.oees.oees.enums.AttemptStatus;
import com.oees.oees.enums.QuestionType;
import com.oees.oees.repository.StudentAttemptRepository;
import com.oees.oees.repository.StudentResponseRepository;
import com.oees.oees.repository.userRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final StudentResponseRepository responseRepository;
    private final StudentAttemptRepository attemptRepository;
    private final userRepository userRepository;

    public List<StudentResponse> getPendingDescriptive(Long attemptId) {
        return responseRepository.findUnevaluatedDescriptive(attemptId);
    }

    @Transactional
    public void evaluateResponse(EvaluateResponseRequest req, Long instructorId) {
        StudentResponse sr = responseRepository.findById(req.getResponseId())
                .orElseThrow(() -> new RuntimeException("Response not found"));

        if (sr.getQuestion().getType() != QuestionType.DESCRIPTIVE) {
            throw new RuntimeException("Only descriptive questions need manual evaluation");
        }

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int max = sr.getQuestion().getMarks();
        if (req.getMarksAwarded() < 0 || req.getMarksAwarded() > max) {
            throw new RuntimeException("Marks must be between 0 and " + max);
        }

        sr.setMarksAwarded(req.getMarksAwarded());
        sr.setEvaluatedBy(instructor);
        sr.setEvaluatedAt(LocalDateTime.now());
        responseRepository.save(sr);

        // Check if all responses for this attempt are now evaluated
        finalizeAttemptIfComplete(sr.getAttempt().getId());
    }

    private void finalizeAttemptIfComplete(Long attemptId) {
        StudentAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        // Check ALL responses (not just descriptive)
        boolean pending = attempt.getResponses().stream()
                .anyMatch(r -> r.getMarksAwarded() == null);

        if (pending) return;

        // Compute total
        int total = attempt.getResponses().stream()
                .mapToInt(r -> r.getMarksAwarded())
                .sum();

        attempt.setTotalScore(total);
        attempt.setEvaluated(true);
        attempt.setStatus(AttemptStatus.EVALUATED);

        attemptRepository.save(attempt);


    }

//    private void finalizeAttemptIfComplete(Long attemptId) {
//        List<StudentResponse> pending = responseRepository.findUnevaluatedDescriptive(attemptId);
//        if (pending.isEmpty()) {
//            StudentAttempt attempt = attemptRepository.findById(attemptId)
//                    .orElseThrow(() -> new RuntimeException("Attempt not found"));
//            attempt.setStatus(AttemptStatus.EVALUATED);
//            attemptRepository.save(attempt);
//        }
//    }
}
