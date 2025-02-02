package org.advisor.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.advisor.member.entities.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MemberAdminControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())  // Spring Security 통합
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})  // ADMIN 역할로 모의 사용자 설정
    void shouldReturnMemberList() throws Exception {
        mockMvc.perform(get("/admin/member/list"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnMemberDetails() throws Exception {
        String seq = "1"; // 유효한 회원 ID
        mockMvc.perform(get("/admin/member/view/" + seq))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(seq))
                .andExpect(jsonPath("$.name").isNotEmpty()); // name이 비어있지 않은지 확인
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateMember() throws Exception {
        String seq = "1";
        Member member = new Member();
        member.setName("Updated Name");
        member.setEmail("updated@example.com");
        member.setPhone("010-1234-5678");
        member.setPassword("new_password");

        String body = objectMapper.writeValueAsString(member);

        mockMvc.perform(post("/admin/member/save/" + seq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(csrf())) // CSRF 토큰 추가
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com")); // 이메일도 검증
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldQuitMember() throws Exception {
        String seq = "1";
        mockMvc.perform(post("/admin/member/quit/" + seq)
                        .with(csrf())) // CSRF 토큰 추가
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldSearchMembers() throws Exception {
        String name = "John";

        mockMvc.perform(get("/admin/member/search").param("name", name))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value(name));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateMemberRoles() throws Exception {
        String seq = "1";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        String rolesJson = objectMapper.writeValueAsString(roles);

        mockMvc.perform(post("/admin/member/role/" + seq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rolesJson)
                        .with(csrf())) // CSRF 토큰 추가
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldChangeMemberPassword() throws Exception {
        String seq = "1";
        String newPassword = "new_secure_password";

        mockMvc.perform(post("/admin/member/password/" + seq)
                        .param("newPassword", newPassword)
                        .with(csrf())) // CSRF 토큰 추가
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value(newPassword));
    }
}
