package com.oees.oees.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private StudentAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String responseText; // descriptive / fill-in-blank

    private String selectedOption; // MCQ

    private Integer marksAwarded; // null until evaluated

    @ManyToOne
    @JoinColumn(name = "evaluated_by")
    private User evaluatedBy;

    private LocalDateTime evaluatedAt;
}
