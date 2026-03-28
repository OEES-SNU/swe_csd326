package com.oees.oees.repository;

import com.oees.oees.entity.StudentAttempt;
import com.oees.oees.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentAttemptRepository extends JpaRepository<StudentAttempt, Long> {
    int countByExamIdAndStudentId(Long examId, Long studentId);

    Optional<StudentAttempt> findByExamIdAndStudentIdAndStatus(Long examId, Long studentId, AttemptStatus status);

    List<StudentAttempt> findByExamIdAndStatus(Long examId, AttemptStatus status);

    List<StudentAttempt> findByStudentId(Long studentId);
}
