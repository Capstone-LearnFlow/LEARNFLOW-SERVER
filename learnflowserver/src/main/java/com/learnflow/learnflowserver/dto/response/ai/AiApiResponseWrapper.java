package com.learnflow.learnflowserver.dto.response.ai;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Collections;

@Getter
@NoArgsConstructor
public class AiApiResponseWrapper {
    private List<AiReviewResponse> data;

    public List<AiReviewResponse> getData() {
        return data != null ? data : Collections.emptyList();
    }
}