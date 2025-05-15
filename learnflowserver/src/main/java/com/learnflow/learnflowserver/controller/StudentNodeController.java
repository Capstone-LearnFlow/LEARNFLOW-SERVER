package com.learnflow.learnflowserver.controller;

import com.learnflow.learnflowserver.domain.StudentAssignment;
import com.learnflow.learnflowserver.domain.User;
import com.learnflow.learnflowserver.dto.request.NodeCreateRequest;
import com.learnflow.learnflowserver.dto.response.ApiResponse;
import com.learnflow.learnflowserver.dto.response.NodeResponse;
import com.learnflow.learnflowserver.repository.AssignmentRepository;
import com.learnflow.learnflowserver.repository.StudentAssignmentRepository;
import com.learnflow.learnflowserver.service.AuthService;
import com.learnflow.learnflowserver.service.NodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/assignments")
@RequiredArgsConstructor
public class StudentNodeController {

    private final NodeService nodeService;
    private final AuthService authService;
    private final StudentAssignmentRepository studentAssignmentRepository;
    private final AssignmentRepository assignmentRepository;

    @PostMapping("/{assignment_id}/nodes")
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
}
