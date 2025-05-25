package com.learnflow.learnflowserver.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponse {
    private List<String> summaries;
}