package com.learnflow.learnflowserver.controller;

import com.learnflow.learnflowserver.dto.AssignmentDetailDto;
import com.learnflow.learnflowserver.dto.AssignmentSummaryDto;
import com.learnflow.learnflowserver.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Tag(name = "학생 대시보드 관련 API")
public class StudentDashboardController {

    private final StudentService studentService;

    @GetMapping("/assignments")
    @Operation(summary = "과제 목록 조회 API")
    public ResponseEntity<Map<String, Object>> getAssignments(HttpServletRequest request) {
        // 인터셉터에서 설정한 사용자 정보 가져오기
        Map<String, Object> userInfo = (Map<String, Object>) request.getAttribute("user");
        Long studentId = Long.valueOf(userInfo.get("id").toString());

        List<AssignmentSummaryDto> assignments = studentService.getAssignmentsByStudentId(studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", assignments);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments/{assignmentId}")
    @Operation(summary = "과제 상세 조회 API")
    public ResponseEntity<Map<String, Object>> getAssignmentDetail(
            @PathVariable Long assignmentId,
            HttpServletRequest request) {

        Map<String, Object> userInfo = (Map<String, Object>) request.getAttribute("user");
        Long studentId = Long.valueOf(userInfo.get("id").toString());

        AssignmentDetailDto assignmentDetail = studentService.getAssignmentDetail(assignmentId, studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", assignmentDetail);

        return ResponseEntity.ok(response);
    }
}