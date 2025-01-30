package org.advisor.global.libs;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Lazy
@Component
@RequiredArgsConstructor
@Slf4j  // 로깅 기능 추가
public class Utils {
    private final HttpServletRequest request;
    private final MessageSource messageSource;

    /**
     * 메서지 코드로 조회된 문구
     *
     * @param code
     * @return
     */
    public String getMessage(String code) {
        Locale lo = request.getLocale(); // 사용자 요청 헤더(Accept-Language)

        try {
            return messageSource.getMessage(code, null, lo);
        } catch (Exception e) {
            log.error("Error retrieving message for code '{}': {}", code, e.getMessage());
            return code;  // 에러가 발생하면 코드 자체를 반환
        }
    }

    /**
     * 다수의 메시지 코드로 조회된 문구 리스트
     *
     * @param codes
     * @return
     */
    public List<String> getMessages(String[] codes) {
        return Arrays.stream(codes).map(code -> {
            try {
                return getMessage(code);
            } catch (Exception e) {
                log.warn("Failed to get message for code '{}': {}", code, e.getMessage());
                return "";
            }
        }).filter(s -> !s.isBlank()).toList();
    }

    /**
     * REST 커맨드 객체 검증 실패시에 에러 코드를 가지고 메시지 추출
     *
     * @param errors
     * @return
     */
    public Map<String, List<String>> getErrorMessages(Errors errors) {
        // 필드별 에러코드 - getFieldErrors()
        Map<String, List<String>> messages = errors.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, f -> getMessages(f.getCodes()), (v1, v2) -> v2));

        // 글로벌 에러코드 - getGlobalErrors()
        List<String> globalMessages = errors.getGlobalErrors()
                .stream()
                .flatMap(o -> getMessages(o.getCodes()).stream())
                .toList();

        // 글로벌 에러코드 필드 - "global"
        if (!globalMessages.isEmpty()) {
            messages.put("global", globalMessages);
        }

        return messages;
    }

    /**
     * 요청 헤더에서 인증 토큰을 추출
     *
     * @return 토큰
     */
    public String getAuthToken() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);  // "Bearer " 제거하고 토큰만 반환
            log.debug("Extracted token: {}", token);
            return token;
        }
        log.warn("No Authorization header or invalid format");
        return null;
    }

    /**
     * 서비스 URL을 생성하는 메서드 (예: "member-service", "/path")
     * @param serviceName 서비스 이름
     * @param path 경로
     * @return 생성된 URL
     */
    public String serviceUrl(String serviceName, String path) {
        String baseUrl = "http://" + serviceName + "/api";  // 기본 URL 생성
        String url = baseUrl + path;  // 경로 추가

        log.debug("Generated service URL: {}", url);
        return url;
    }
}
