
package org.advisor.member.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.advisor.global.libs.Utils;
import org.advisor.member.constants.TockenAction;
import org.advisor.member.entities.Member;
import org.advisor.member.entities.TempToken;
import org.advisor.member.exceptions.MemberNotFoundException;
import org.advisor.member.exceptions.TempTokenExpiredException;
import org.advisor.member.exceptions.TempTokenNotFoundException;
import org.advisor.member.repositories.MemberRepository;
import org.advisor.member.repositories.TempTokenRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Lazy
@Service
@RequiredArgsConstructor
public class TempTokenService {

    private final MemberRepository memberRepository;
    private final TempTokenRepository tempTokenRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper om;
    private final Utils utils;


    /**
     * 임시 접근 토큰 발급
     *
     * @return
     */
    public TempToken issue(String email, TockenAction action, String origin) {
        Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);

        TempToken token = TempToken.builder()
                .token(UUID.randomUUID().toString())
                .member(member)
                .action(action)
                .expireTime(LocalDateTime.now().plusMinutes(3L))
                .origin(origin) // 유입된 프론트앤드 도메인 주소 예) https://pintech.koreait.xyz
                .build();

        tempTokenRepository.saveAndFlush(token);

        return token;
    }

    /**
     * 발급 받은 토큰으로 접근 가능한 주소 생성 후 메일 전송
     *
     * @param token
     */
    public boolean sendEmail(String token) {
        TempToken tempToken = get(token);

        Member member = tempToken.getMember();
        String email = member.getEmail();

        String tokenUrl = tempToken.getOrigin() + tempToken.getToken();
        String subject = tempToken.getAction() == TockenAction.PASSWORD_CHANGE ? "비밀번호 변경 안내입니다.":"....";

        Map<String, String> data = new HashMap<>();
        data.put("to", email);
        data.put("subject", subject);
        data.put("content", tokenUrl);

        try {
            String emailUrl = utils.serviceUrl("email-service", "/tpl/general");
            String params = om.writeValueAsString(data);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(params, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity(URI.create(emailUrl), request, Void.class);

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public TempToken get(String token) {
        TempToken tempToken = tempTokenRepository.findByToken(token).orElseThrow(TempTokenNotFoundException::new);
        if (tempToken.isExpired()) {
            throw new TempTokenExpiredException();
        }

        return tempToken;
    }
}