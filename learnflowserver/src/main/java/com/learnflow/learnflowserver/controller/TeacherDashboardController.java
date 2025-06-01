package com.learnflow.learnflowserver.controller;

import com.learnflow.learnflowserver.dto.AssignmentSummaryForTeacherDto;
import com.learnflow.learnflowserver.dto.StudentDto;
import com.learnflow.learnflowserver.dto.request.AssignmentCreateRequest;
import com.learnflow.learnflowserver.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Tag(name = "선생님 대시보드 관련 API")
public class TeacherDashboardController {

    private final TeacherService teacherService;

    @GetMapping("/students")
    @Operation(summary = "학생 목록 조회 API")
    public ResponseEntity<Map<String, Object>> getStudents() {
        List<StudentDto> students = teacherService.getAllStudents();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", students);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/assignments")
    @Operation(summary = "과제 생성 API")
    public ResponseEntity<Map<String, Object>> createAssignment(
            @Valid @RequestBody AssignmentCreateRequest request,
            HttpServletRequest httpRequest) {

        Map<String, Object> userInfo = (Map<String, Object>) httpRequest.getAttribute("user");
        Long teacherId = Long.valueOf(userInfo.get("id").toString());

        Long assignmentId = teacherService.createAssignment(request, teacherId);

        Map<String, Object> data = new HashMap<>();
        data.put("assignment_id", assignmentId);
        data.put("message", "과제가 성공적으로 생성되었습니다.");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignments")
    @Operation(summary = "교사 과제 목록 조회 API")
    public ResponseEntity<Map<String, Object>> getAssignments(HttpServletRequest request) {
        Map<String, Object> userInfo = (Map<String, Object>) request.getAttribute("user");
        Long teacherId = Long.valueOf(userInfo.get("id").toString());

        List<AssignmentSummaryForTeacherDto> assignments = teacherService.getAssignmentsByTeacherId(teacherId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", assignments);

        return ResponseEntity.ok(response);
    }
}