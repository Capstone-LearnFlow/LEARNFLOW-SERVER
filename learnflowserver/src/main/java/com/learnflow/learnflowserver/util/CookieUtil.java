package com.learnflow.learnflowserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final ObjectMapper objectMapper;
    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7일
    private static final String COOKIE_PATH = "/";

    public void addCookie(HttpServletResponse response, String name, Map<String, Object> value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            String encodedValue = Base64.getEncoder().encodeToString(jsonValue.getBytes());

            Cookie cookie = new Cookie(name, encodedValue);
            cookie.setPath(COOKIE_PATH);
            cookie.setMaxAge(COOKIE_MAX_AGE);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("쿠키 생성 중 오류가 발생했습니다.", e);
        }
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst();
    }

    public Optional<Map<String, Object>> getCookieValue(HttpServletRequest request, String name) {
        return getCookie(request, name).flatMap(cookie -> {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(cookie.getValue());
                String decodedValue = new String(decodedBytes);
                Map<String, Object> map = objectMapper.readValue(decodedValue, new TypeReference<Map<String, Object>>() {});
                return Optional.of(map);
            } catch (Exception e) {
                return Optional.empty(); // 예외 시 Optional.empty() 반환
            }
        });

    }
}