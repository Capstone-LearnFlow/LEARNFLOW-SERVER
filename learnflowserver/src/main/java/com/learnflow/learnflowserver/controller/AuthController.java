package com.learnflow.learnflowserver.controller;

import com.learnflow.learnflowserver.dto.request.LoginRequest;
import com.learnflow.learnflowserver.dto.response.LoginResponse;
import com.learnflow.learnflowserver.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인 API")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request.getNumber(), response);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("data", loginResponse);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        authService.logout(response);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "로그아웃 되었습니다.");

        return ResponseEntity.ok(result);
    }
}