package com.learnflow.learnflowserver.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "phases")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Phase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @Column(nullable = false)
    private Integer phaseNumber;  // 1, 2, 3, 4

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;  // INACTIVE / ACTIVE / COMPLETED

    public Phase(Assignment assignment, Integer phaseNumber, LocalDateTime startDate, LocalDateTime endDate, Status status) {
        this.assignment = assignment;
        this.phaseNumber = phaseNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
}