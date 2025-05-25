package com.learnflow.learnflowserver.dto.request.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryRequest {
    private List<String> contents;

    public static SummaryRequest of(List<String> contents) {
        return SummaryRequest.builder()
                .contents(contents)
                .build();
    }
}
