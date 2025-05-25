package com.learnflow.learnflowserver.domain;

import com.learnflow.learnflowserver.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String source;

    private String url;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    public enum ResourceType {
        ARTICLE, RESEARCH, STATISTICS, OTHER
    }
}