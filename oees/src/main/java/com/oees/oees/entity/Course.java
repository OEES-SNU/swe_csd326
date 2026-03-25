package com.oees.oees.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String courseCode;

    @Column(nullable = false)
    private String courseName;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @Column(nullable = false)
    private boolean active = true;
}
