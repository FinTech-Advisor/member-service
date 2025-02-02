package org.advisor.global.interceptors;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No Authorization header or invalid format");
            return false; // 요청 중단
        }

        String token = authorizationHeader.substring(7);

        // 토큰 검증
        if (!validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false; // 요청 중단
        }

        return true; // 다음 단계 진행
    }

    private boolean validateToken(String token) {
        // JWT 검증 로직 추가
        return token.equals("validToken"); // 예제: 간단한 검증
    }
}
