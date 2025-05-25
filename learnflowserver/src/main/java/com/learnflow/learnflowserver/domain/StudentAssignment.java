package com.learnflow.learnflowserver.domain;

import com.learnflow.learnflowserver.domain.common.BaseEntity;
import com.learnflow.learnflowserver.domain.common.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_assignments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @Column(nullable = false)
    private Integer currentPhase;  // 1~4

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StudentStatus status;  // NOT_STARTED / IN_PROGRESS / COMPLETED

    public StudentAssignment(User student, Assignment assignment, Integer currentPhase, StudentStatus status) {
        this.student = student;
        this.assignment = assignment;
        this.currentPhase = currentPhase;
        this.status = status;
    }
}