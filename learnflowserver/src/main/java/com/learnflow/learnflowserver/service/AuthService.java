package com.learnflow.learnflowserver.service;

import com.learnflow.learnflowserver.dto.response.LoginResponse;
import com.learnflow.learnflowserver.entity.User;
import com.learnflow.learnflowserver.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.learnflow.learnflowserver.util.CookieUtil;

import java.util.HashMap;
import java.util.Map;

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
}