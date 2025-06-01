package com.learnflow.learnflowserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreeNodeResponse {
    private String type; // "반론" | "질문"
    private String content;
    private String summary;
    private List<TreeNodeResponse> child; // type이 "반론"일 때만 포함
}