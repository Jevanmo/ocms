package com.ocms.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"assignment_id", "student_id"})
})
@Getter
@Setter
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Assignment assignment;

    @ManyToOne(optional = false)
    private User student;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    private Integer grade;
    private String feedback;
    private LocalDateTime submittedAt;
}
