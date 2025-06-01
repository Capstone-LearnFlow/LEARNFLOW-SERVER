package com.learnflow.learnflowserver.controller;

import com.learnflow.learnflowserver.domain.User;
import com.learnflow.learnflowserver.domain.common.enums.Role;
import com.learnflow.learnflowserver.dto.response.ApiResponse;
import com.learnflow.learnflowserver.dto.response.StudentTreeLogResponse;
import com.learnflow.learnflowserver.service.AuthService;
import com.learnflow.learnflowserver.service.NodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/teacher/assignments")
@RequiredArgsConstructor
@Tag(name = "교사용 트리 로그 API")
public class TeacherTreeLogController {

    private final NodeService nodeService;
    private final AuthService authService;

    @GetMapping("/{assignment_id}/students/{student_id}/tree-logs")
    @Operation(summary = "학생별 트리 생성 로그 조회")
    public ResponseEntity<ApiResponse<StudentTreeLogResponse>> getStudentTreeLogs(
            @PathVariable("assignment_id") Long assignmentId,
            @PathVariable("student_id") Long studentId) {

        // 교사 권한 확인
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("교사만 접근할 수 있습니다.");
        }

        StudentTreeLogResponse response = nodeService.getStudentTreeLogs(assignmentId, studentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{assignment_id}/tree-logs/summary")
    @Operation(summary = "과제별 전체 학생 활동 요약 조회")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTreeLogsSummary(
            @PathVariable("assignment_id") Long assignmentId,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 교사 권한 확인
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.TEACHER) {
            throw new IllegalArgumentException("교사만 접근할 수 있습니다.");
        }

        Map<String, Object> response = nodeService.getTreeLogsSummary(assignmentId, sort, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}