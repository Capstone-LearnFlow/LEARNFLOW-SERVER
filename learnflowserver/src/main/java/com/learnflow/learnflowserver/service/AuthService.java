package com.learnflow.learnflowserver.service;

import com.learnflow.learnflowserver.dto.response.LoginResponse;
import com.learnflow.learnflowserver.domain.User;
import com.learnflow.learnflowserver.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.learnflow.learnflowserver.util.CookieUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    public LoginResponse login(String number, HttpServletResponse response) {
        // 학번 또는 아이디로 사용자 조회
        User user = userRepository.findByNumber(number)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 쿠키에 사용자 정보 저장
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("role", user.getRole().name());

        cookieUtil.addCookie(response, "user", userInfo);

        return LoginResponse.from(user.getId(), user.getName(), user.getRole());
    }

    public void logout(HttpServletResponse response) {
        cookieUtil.deleteCookie(response, "user");
    }

    public User getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("현재 요청 컨텍스트를 찾을 수 없습니다.");
        }

        HttpServletRequest request = attributes.getRequest();
        Optional<Map<String, Object>> userInfo = cookieUtil.getCookieValue(request, "user");

        if (userInfo.isEmpty()) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }

        Long userId = ((Number) userInfo.get().get("id")).longValue();

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));
    }

}