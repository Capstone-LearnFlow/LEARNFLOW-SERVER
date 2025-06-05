package com.learnflow.learnflowserver.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NodeUpdateRequest {
    private String content;
    private List<EvidenceUpdateRequest> evidences;
}
