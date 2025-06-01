package com.learnflow.learnflowserver.config;

import com.learnflow.learnflowserver.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final CookieUtil cookieUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs") || uri.equals("/swagger-ui.html")) {
            return true; // Swagger는 인터셉터 무시
        }

        // 쿠키에서 사용자 정보 확인
        Optional<Map<String, Object>> userInfo = cookieUtil.getCookieValue(request, "user");

        if (userInfo.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("{\"status\":\"error\",\"message\":\"로그인이 필요합니다.\"}");
            return false;
        }

        // 교사 전용 API 엔드포인트 체크 (선택적)
        String path = uri;
        if (path.startsWith("/api/teacher")
                && !"TEACHER".equals(userInfo.get().get("role"))) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("{\"status\":\"error\",\"message\":\"접근 권한이 없습니다.\"}");
            return false;
        }

        // 학생 전용 API 엔드포인트 체크 (선택적)
        if (path.startsWith("/api/student")
                && !"STUDENT".equals(userInfo.get().get("role"))) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("{\"status\":\"error\",\"message\":\"접근 권한이 없습니다.\"}");
            return false;
        }

        // 사용자 정보를 request에 속성으로 추가
        request.setAttribute("user", userInfo.get());
        return true;
    }
}