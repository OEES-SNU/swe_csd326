package com.oees.oees.entity;

import com.oees.oees.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "student_attempts", uniqueConstraints = @UniqueConstraint(columnNames = { "exam_id", "student_id",
        "attempt_number" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    private Integer attemptNumber;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL)
    private List<StudentResponse> responses;

    private Integer totalScore;
    @Builder.Default
    private Boolean evaluated = false;
}
