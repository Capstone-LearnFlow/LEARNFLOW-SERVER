package com.learnflow.learnflowserver.dto.response.ai;

import com.learnflow.learnflowserver.dto.response.NodeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class StudentResponseWithAiResponse {
    private NodeResponse studentResponse;
    private List<NodeResponse> aiResponses;

    // 편의 메서드
    public boolean hasAiResponses() {
        return aiResponses != null && !aiResponses.isEmpty();
    }

    public int getAiResponseCount() {
        return aiResponses != null ? aiResponses.size() : 0;
    }
}