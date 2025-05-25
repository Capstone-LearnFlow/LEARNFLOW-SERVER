package com.learnflow.learnflowserver.domain;

import com.learnflow.learnflowserver.domain.common.BaseEntity;
import com.learnflow.learnflowserver.domain.common.enums.CreatedBy;
import com.learnflow.learnflowserver.domain.common.enums.NodeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Node extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_assignment_id")
    private StudentAssignment studentAssignment;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Node parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Node> children = new ArrayList<>();

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evidence> evidences = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private NodeType type;

    @Enumerated(EnumType.STRING)
    private CreatedBy createdBy;

    private boolean isHidden;

    // 연관관계 편의 메서드
    public void addChild(Node child) {
        this.children.add(child);
        child.setParent(this);
    }

    private void setParent(Node parent) {
        this.parent = parent;
    }

    public void setStudentAssignment(StudentAssignment studentAssignment) {
        this.studentAssignment = studentAssignment;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}