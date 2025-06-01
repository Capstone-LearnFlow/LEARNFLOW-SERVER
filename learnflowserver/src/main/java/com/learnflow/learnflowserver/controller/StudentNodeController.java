package com.learnflow.learnflowserver.controller;

import com.learnflow.learnflowserver.domain.StudentAssignment;
import com.learnflow.learnflowserver.domain.User;
import com.learnflow.learnflowserver.dto.request.NodeCreateRequest;
import com.learnflow.learnflowserver.dto.response.ApiResponse;
import com.learnflow.learnflowserver.dto.response.NodeDetailResponse;
import com.learnflow.learnflowserver.dto.response.NodeResponse;
import com.learnflow.learnflowserver.dto.response.NodeTreeResponse;
import com.learnflow.learnflowserver.repository.AssignmentRepository;
import com.learnflow.learnflowserver.repository.StudentAssignmentRepository;
import com.learnflow.learnflowserver.service.AuthService;
import com.learnflow.learnflowserver.service.NodeService;
import com.learnflow.learnflowserver.service.ai.AiReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/assignments")
@RequiredArgsConstructor
@Tag(name = "2단계 트리 관련 API")
public class StudentNodeController {

    private final NodeService nodeService;
    private final AuthService authService;
    private final StudentAssignmentRepository studentAssignmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AiReviewService aiReviewService;

    @PostMapping("/{assignment_id}/nodes")
    @Operation(summary = "메인 노드 생성 API")
    public ResponseEntity<ApiResponse<NodeResponse>> createMainNode(
            @PathVariable("assignment_id") Long assignmentId,
            @RequestBody NodeCreateRequest request) {

        // 현재 인증된 학생 정보 가져오기
        User currentUser = authService.getCurrentUser();

        // 해당 학생과 과제의 연결 정보 조회
        StudentAssignment studentAssignment = studentAssignmentRepository.findByStudentIdAndAssignmentId(
                        currentUser.getId(), assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생에게 할당된 과제를 찾을 수 없습니다."));

        // 메인 노드 생성
        NodeResponse response = nodeService.createMainNode(studentAssignment.getId(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{assignment_id}/nodes/{node_id}")
    @Operation(summary = "노드 상세 조회 API")
    public ResponseEntity<ApiResponse<NodeDetailResponse>> getNodeDetail(
            @PathVariable("assignment_id") Long assignmentId,
            @PathVariable("node_id") Long nodeId) {

        // 현재 인증된 학생 정보 가져오기
        User currentUser = authService.getCurrentUser();

        // 해당 학생과 과제의 연결 정보 확인 (접근 권한 검증)
        studentAssignmentRepository.findByStudentIdAndAssignmentId(
                        currentUser.getId(), assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생에게 할당된 과제를 찾을 수 없습니다."));

        // 노드 상세 정보 조회
        NodeDetailResponse response = nodeService.getNodeDetail(nodeId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{assignment_id}/nodes/tree")
    @Operation(summary = "전체 트리 구조 조회 API")
    public ResponseEntity<ApiResponse<NodeTreeResponse>> getNodeTree(
            @PathVariable("assignment_id") Long assignmentId) {

        User currentUser = authService.getCurrentUser();
        StudentAssignment studentAssignment = studentAssignmentRepository.findByStudentIdAndAssignmentId(
                        currentUser.getId(), assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학생에게 할당된 과제를 찾을 수 없습니다."));

        NodeTreeResponse response = nodeService.getNodeTree(studentAssignment.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{assignment_id}/ai-response")
    @Operation(summary = "AI반론, 질문 생성 API")
    public ResponseEntity<ApiResponse<?>> generateAiResponse(
            @PathVariable("assignment_id") Long assignmentId,
            @RequestBody Map<String, Object> request) {

        try {
            User currentUser = authService.getCurrentUser();
            StudentAssignment studentAssignment = studentAssignmentRepository.findByStudentIdAndAssignmentId(
                            currentUser.getId(), assignmentId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 학생에게 할당된 과제를 찾을 수 없습니다."));

            Integer reviewNum = (Integer) request.getOrDefault("review_num", 1);

            List<NodeResponse> responses = aiReviewService.generateAiResponse(studentAssignment.getId(), reviewNum);

            return ResponseEntity.ok(ApiResponse.success(responses));
        } catch (Exception e) {
            System.err.println("AI 응답 생성 API 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error("AI 응답 생성에 실패했습니다: " + e.getMessage()));
        }
    }
}
