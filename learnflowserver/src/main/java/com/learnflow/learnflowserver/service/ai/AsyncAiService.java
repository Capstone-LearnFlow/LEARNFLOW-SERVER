package com.learnflow.learnflowserver.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsyncAiService {

    private final AiReviewService aiReviewService;

    @Async("aiTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateAiResponseAsync(Long studentAssignmentId) {
        try {
            System.out.println("=== 비동기 AI 응답 생성 시작 ===");
            System.out.println("Thread: " + Thread.currentThread().getName());
            System.out.println("StudentAssignment ID: " + studentAssignmentId);

            aiReviewService.generateAiResponse(studentAssignmentId, 1);

            System.out.println("=== 비동기 AI 응답 생성 완료 ===");
            System.out.println("StudentAssignment ID: " + studentAssignmentId);
        } catch (Exception e) {
            System.err.println("=== 비동기 AI 응답 생성 실패 ===");
            System.err.println("StudentAssignment ID: " + studentAssignmentId);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}