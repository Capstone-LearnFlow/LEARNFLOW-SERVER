package com.learnflow.learnflowserver.service.ai;

import com.learnflow.learnflowserver.dto.request.ai.AiReviewRequest;
import com.learnflow.learnflowserver.dto.request.ai.SummaryRequest;
import com.learnflow.learnflowserver.dto.response.ai.AiApiResponseWrapper;
import com.learnflow.learnflowserver.dto.response.ai.AiReviewResponse;
import com.learnflow.learnflowserver.dto.response.ai.SummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.api.base-url}")
    private String baseUrl;

    public List<String> getSummaries(List<String> contents) {
        String url = baseUrl + "/summary";
        SummaryRequest request = SummaryRequest.of(contents);
        SummaryResponse response = restTemplate.postForObject(url, request, SummaryResponse.class);
        return response.getSummaries();
    }

    public List<AiReviewResponse> getReviews(AiReviewRequest request) {
        String url = baseUrl + "/review";
        AiApiResponseWrapper wrapper = restTemplate.postForObject(url, request, AiApiResponseWrapper.class);
        if (wrapper != null) {
            return wrapper.getData();
        }
        return Collections.emptyList();
    }
}