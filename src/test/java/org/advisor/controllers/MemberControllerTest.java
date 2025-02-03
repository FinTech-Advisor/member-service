package org.advisor.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.advisor.member.controllers.RequestFindPassword;
import org.advisor.member.controllers.RequestJoin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles({"default", "test", "jwt"})
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void joinTest() throws Exception {
        // 회원가입 요청 데이터
        RequestJoin form = new RequestJoin();
        form.setId("user01");
        form.setEmail("user01@test.org");
        form.setName("사용자01");
        form.setPassword("_aA123456");
        form.setConfirmPassword("_aA123456");
        form.setMobile("010-1234-5678");
        form.setRequiredTerms1(true);
        form.setRequiredTerms2(true);
        form.setRequiredTerms3(true);
        form.setOptionalTerms(Collections.singletonList("advertisement"));

        // JSON 직렬화
        String body = objectMapper.writeValueAsString(form);

        // 상태 코드만 검증
        mockMvc.perform(post("/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated()); // 201 상태 코드 검증
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
        // USER 역할로 모의 사용자 설정
    void shouldReturnBadRequestForInvalidForm() throws Exception {
        // 잘못된 RequestFindPassword 데이터를 준비
        RequestFindPassword form = new RequestFindPassword();
        form.setEmail(""); // 이메일을 빈 문자열로 설정 (유효성 검사에 실패하게 만듦)

        // 유효성 검사 실패 시 발생하는 BadRequestException을 확인
        mockMvc.perform(post("/find/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 상태코드 400 확인
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("유효성 검사 오류 메시지"));  // ErrorResponse 메시지 검증
    }

    @Test
    @WithMockUser(username = "member", roles = {"USER"})
        // USER 역할로 설정
    void shouldReturnNoContentForValidForm() throws Exception {
        // 유효한 RequestFindPassword 데이터를 준비
        RequestFindPassword form = new RequestFindPassword();
        form.setEmail("valid@example.com"); // 유효한 이메일

        // 유효한 폼 제출 시, NO_CONTENT 상태 코드 확인
        mockMvc.perform(post("/find/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andDo(print())
                .andExpect(status().isNoContent()); // 상태코드 204 확인
    }
}