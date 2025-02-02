package org.advisor.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.advisor.member.controllers.RequestJoin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

}
