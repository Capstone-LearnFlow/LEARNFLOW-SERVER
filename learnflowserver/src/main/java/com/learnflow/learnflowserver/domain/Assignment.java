package com.learnflow.learnflowserver.domain;

import com.learnflow.learnflowserver.domain.common.BaseEntity;
import com.learnflow.learnflowserver.domain.common.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String chapter;

    @Column(nullable = false)
    private String topic;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;    // INACTIVE / ACTIVE / COMPLETED

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;   // 교사

    public Assignment(String subject, String chapter, String topic, String description, Status status, User createdBy) {
        this.subject = subject;
        this.chapter = chapter;
        this.topic = topic;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
    }
}