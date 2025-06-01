package com.learnflow.learnflowserver.dto.response.ai;

import com.learnflow.learnflowserver.dto.response.TreeNodeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReviewResponse {
    private String parent; // 리뷰가 생성된 대상 노드의 id
    private TreeNodeResponse tree;
}