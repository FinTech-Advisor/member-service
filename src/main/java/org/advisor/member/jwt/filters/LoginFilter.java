package org.advisor.member.jwt.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.advisor.global.exceptions.UnAuthorizedException;
import org.advisor.global.libs.Utils;
import org.advisor.global.rests.JSONData;
import org.advisor.member.entities.Member;
import org.advisor.member.jwt.TokenService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

    private final TokenService tokenService;
    private final Utils utils;  // 다른 서비스들의 URL을 가져오기 위한 Utils 클래스
    private final RestTemplate restTemplate;  // RestTemplate을 통해 API 호출
    private final ObjectMapper om;  // JSON 변환을 위한 ObjectMapper

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 토큰이 유입되면 로그인 처리
        try {
            // 인증을 수행합니다.
            tokenService.authenticate((HttpServletRequest) request);

            // 인증 후 추가 작업 수행
            String token = utils.getAuthToken(); // 토큰을 추출 (이미 인증이 되어있다면)
            loginProcess(token); // 회원 정보 확인 및 후속 처리

        } catch (UnAuthorizedException e) {
            logger.error("Unauthorized access: {}", e.getMessage());
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage());
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error.");
        }

        // 필터 체인 진행
        chain.doFilter(request, response);
    }

    /**
     * 로그인 처리 후, 추가 작업을 수행하는 메서드
     * - 이메일 전송, 보드 정보 조회, 메시지 전송 등의 작업을 처리
     */
    private void loginProcess(String token) {
        if (token == null || token.isEmpty()) {
            return;  // 토큰이 없으면 처리할 필요 없음
        }

        try {
            // 1. member-service에서 회원 정보를 조회
            String apiUrl = utils.serviceUrl("member-service", "/");

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<JSONData> response = restTemplate.exchange(apiUrl, HttpMethod.GET, request, JSONData.class);
            JSONData jsonData = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && jsonData != null && jsonData.isSuccess()) {
                // 응답이 성공적이면 회원 정보를 추출
                String json = om.writeValueAsString(jsonData.getData());
                Member member = om.readValue(json, Member.class);

                // 이후, 각 서비스로 필요한 요청을 보냄
                // 2. 이메일 전송
                sendWelcomeEmail(member);

                // 3. 보드 정보 조회
                getBoardInfo(member);

                // 4. 메시지 전송
                sendMessage(member);

            } else {
                logger.error("Failed to retrieve member information");
            }

        } catch (HttpClientErrorException | IOException e) {
            // API 호출 실패 시 예외 처리
            logger.error("Error during API call: {}", e.getMessage());
        }
    }

    // 이메일 전송 (email-service)
    private void sendWelcomeEmail(Member member) {
        try {
            String emailServiceUrl = utils.serviceUrl("email-service", "/send-email");

            String emailJson = String.format("{\"to\": \"%s\", \"subject\": \"Welcome!\", \"body\": \"Hello %s, welcome to our platform!\"}",
                    member.getEmail(), member.getName());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> request = new HttpEntity<>(emailJson, headers);
            ResponseEntity<JSONData> response = restTemplate.exchange(emailServiceUrl, HttpMethod.POST, request, JSONData.class);

            JSONData jsonData = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && jsonData != null && jsonData.isSuccess()) {
                logger.info("Email sent successfully to {}", member.getEmail());
            } else {
                logger.error("Failed to send email to {}", member.getEmail());
            }
        } catch (Exception e) {
            logger.error("Error sending email: {}", e.getMessage());
        }
    }

    // 보드 정보 조회 (board-service)
    private void getBoardInfo(Member member) {
        try {
            String boardServiceUrl = utils.serviceUrl("board-service", "/get-board-info");

            String requestJson = String.format("{\"userId\": \"%s\"}", member.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
            ResponseEntity<JSONData> response = restTemplate.exchange(boardServiceUrl, HttpMethod.POST, request, JSONData.class);

            JSONData jsonData = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && jsonData != null && jsonData.isSuccess()) {
                logger.info("Board Info: {}", jsonData.getData());
            } else {
                logger.error("Failed to retrieve board information");
            }
        } catch (Exception e) {
            logger.error("Error retrieving board info: {}", e.getMessage());
        }
    }

    // 메시지 전송 (message-service)
    private void sendMessage(Member member) {
        try {
            String messageServiceUrl = utils.serviceUrl("message-service", "/send-message");

            String messageJson = String.format("{\"sender\": \"%s\", \"recipient\": \"%s\", \"message\": \"Hello %s, here's your message!\"}",
                    member.getId(), "recipientId", member.getName());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> request = new HttpEntity<>(messageJson, headers);
            ResponseEntity<JSONData> response = restTemplate.exchange(messageServiceUrl, HttpMethod.POST, request, JSONData.class);

            JSONData jsonData = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && jsonData != null && jsonData.isSuccess()) {
                logger.info("Message sent successfully to recipientId");
            } else {
                logger.error("Failed to send message");
            }
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage());
        }
    }
}
