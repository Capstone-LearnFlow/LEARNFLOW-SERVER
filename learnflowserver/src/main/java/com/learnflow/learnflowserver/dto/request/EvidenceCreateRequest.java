package com.learnflow.learnflowserver.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceCreateRequest {
    private String content;
    private String source;
    private String url;
}