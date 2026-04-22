package com.oees.oees.entity;

import com.oees.oees.enums.ExamStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    private Integer durationMinutes;
    private Integer totalMarks;
    private Integer maxAttempts;
    private Integer passMark;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExamStatus status = ExamStatus.DRAFT;

    @Convert(converter = GradingScaleConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, Integer> gradingScale;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamQuestion> examQuestions;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
