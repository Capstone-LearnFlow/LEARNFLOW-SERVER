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
    @JoinColumn(name = "student_assignment_id", nullable = false)
    private StudentAssignment studentAssignment;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Node parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Node> children = new ArrayList<>();

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Evidence> evidences = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_evidence_id", nullable = true) // 이 노드를 촉발시킨 근거 (선택적)
    private Evidence triggeredByEvidence;

    @Enumerated(EnumType.STRING)
    private NodeType type;

    @Enumerated(EnumType.STRING)
    private CreatedBy createdBy;

    private boolean isHidden = false;

    // 연관관계 편의 메서드
    public void addChild(Node child) {
        this.children.add(child);
        child.setParentNode(this);
    }

    private void setParentNode(Node parent) {
        this.parent = parent;
    }

    public void addEvidence(Evidence evidence) {
        if (this.evidences == null) {
            this.evidences = new ArrayList<>();
        }
        this.evidences.add(evidence);
        evidence.setNode(this);
    }

    public List<Evidence> getEvidences() {
        if (this.evidences == null) {
            this.evidences = new ArrayList<>();
        }
        return evidences;
    }

}