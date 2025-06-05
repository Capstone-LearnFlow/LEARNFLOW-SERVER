package com.learnflow.learnflowserver.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceUpdateRequest {
    private Long id; // null이면 새로 생성, 있으면 수정
    private String content;
    private String source;
    private String url;
}