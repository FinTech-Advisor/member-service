package org.advisor.services;

import org.advisor.global.exceptions.BadRequestException;
import org.advisor.global.libs.Utils;
import org.advisor.member.constants.Status;
import org.advisor.member.controllers.RequestChangePassword;
import org.advisor.member.controllers.RequestFindPassword;
import org.advisor.member.entities.Member;
import org.advisor.member.entities.TempToken;
import org.advisor.member.repositories.AuthoritiesRepository;
import org.advisor.member.repositories.MemberRepository;
import org.advisor.member.services.MemberUpdateService;
import org.advisor.member.services.TempTokenService;
import org.advisor.member.validators.MemberUpdateValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberUpdateServiceTest {

    private static final String MEMBER_ID = "member123";
    private static final String INVALID_MEMBER_ID = "nonexistentMember";

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AuthoritiesRepository authoritiesRepository;

    @Mock
    private MemberUpdateValidator memberUpdateValidator;

    @Mock
    private Utils utils;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TempTokenService tempTokenService;
    @InjectMocks
    private MemberUpdateService memberUpdateService;
    private TempToken tempToken;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId(MEMBER_ID);
        testMember.setName("John Doe");
        testMember.setEmail("johndoe@example.com");
        testMember.setMobile("010-1234-5678");
        testMember.setPassword("password123");
        testMember.setConfirmPassword("password123");
        testMember.setRequiredTerms1(true);
        testMember.setRequiredTerms2(true);
        testMember.setRequiredTerms3(true);
        testMember.setOptionalTerms("");
        testMember.setCredentialChangedAt(LocalDateTime.now());
        testMember.setStatus(Status.ACTIVE);
        tempToken = new TempToken();
        tempToken.setToken("validToken");
        tempToken.setMember(testMember);
    }

    @Test
    void testGetAllMembers() {
        when(memberRepository.findAll()).thenReturn(List.of(testMember));

        var members = memberUpdateService.getAllMembers();

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(MEMBER_ID, members.get(0).getId());
    }

    @Test
    void testUpdateMemberProfile_Success() {
        String updatedName = "Jane Doe";
        String updatedEmail = "janedoe@example.com";
        String updatedPhone = "010-2345-6789";
        String updatedPassword = "newpassword123";

        when(passwordEncoder.encode(updatedPassword)).thenReturn("hashedPassword123");
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member updatedMember = memberUpdateService.updateMemberProfile(
                MEMBER_ID, updatedName, updatedEmail, updatedPhone, updatedPassword);

        assertNotNull(updatedMember);
        assertEquals(updatedName, updatedMember.getName());
        assertEquals(updatedEmail, updatedMember.getEmail());
        assertEquals(updatedPhone, updatedMember.getMobile());
        assertEquals("hashedPassword123", updatedMember.getPassword());
    }

    @Test
    void testUpdateMemberRoles_Failure_InvalidRole() {
        List<String> newRoles = List.of("INVALID_ROLE");

        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(testMember));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberUpdateService.updateMemberRoles(MEMBER_ID, newRoles);
        });

        assertEquals("Invalid role: INVALID_ROLE", exception.getMessage());
    }


    @Test
    void testUpdateMemberProfile_Failure_MemberNotFound() {
        String updatedName = "Jane Doe";
        String updatedEmail = "janedoe@example.com";
        String updatedPhone = "010-2345-6789";
        String updatedPassword = "newpassword123";

        when(memberRepository.findById(INVALID_MEMBER_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberUpdateService.updateMemberProfile(
                    INVALID_MEMBER_ID, updatedName, updatedEmail, updatedPhone, updatedPassword);
        });

        assertEquals("Member not found with id: " + INVALID_MEMBER_ID, exception.getMessage());
    }

    @Test
    void testDeleteMember() {
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(testMember));

        memberUpdateService.deleteMember(MEMBER_ID);

        verify(memberRepository, times(1)).delete(testMember);
    }



    @Test
    void testUpdateMemberRoles() {
        List<String> newRoles = List.of("ADMIN", "USER");

        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(testMember));

        Member updatedMember = memberUpdateService.updateMemberRoles(MEMBER_ID, newRoles);

        assertNotNull(updatedMember);
        verify(authoritiesRepository, times(1)).deleteAllByMember_Id(testMember.getId());
        verify(authoritiesRepository, times(1)).saveAll(any());
    }
    // issueToken 메서드 테스트
    @Test
    void testIssueToken() {
        RequestFindPassword form = new RequestFindPassword();
        form.setName("John Doe");
        form.setMobile("1234567890");

        // 모킹: memberRepository와 tempTokenService
        when(memberRepository.findByNameAndMobile("John Doe", "1234567890")).thenReturn(Optional.of(testMember));
        when(tempTokenService.issue(anyString(), any(), any())).thenReturn(tempToken);

        // issueToken 호출
        memberUpdateService.issueToken(form);

        // 이메일 전송 확인
        verify(tempTokenService).sendEmail("validToken");
    }

    // changePassword 메서드 테스트
    @Test
    void testChangePassword() {
        RequestChangePassword form = new RequestChangePassword();
        form.setToken("validToken");
        form.setPassword("ValidPassword123!"); // 유효한 비밀번호

        // 모킹: tempTokenService
        when(tempTokenService.get("validToken")).thenReturn(tempToken);
        when(passwordEncoder.encode("ValidPassword123!")).thenReturn("encodedPassword");

        // changePassword 호출
        memberUpdateService.changePassword(form);

        // 비밀번호가 변경되었는지 확인
        verify(memberRepository).saveAndFlush(testMember);
        assertEquals("encodedPassword", testMember.getPassword());
        assertNotNull(testMember.getCredentialChangedAt()); // 비밀번호 변경 시간이 갱신되었는지 확인
    }

    // 비밀번호 자리수 검증 실패 테스트
    @Test
    void testChangePasswordWithShortPassword() {
        RequestChangePassword form = new RequestChangePassword();
        form.setToken("validToken");
        form.setPassword("short"); // 비밀번호 자리수가 너무 짧음

        // 모킹: tempTokenService
        when(tempTokenService.get("validToken")).thenReturn(tempToken);

        // 예외 발생 확인
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            memberUpdateService.changePassword(form);
        });

        assertEquals("Size.requestJoin.password", exception.getMessage()); // 메시지 검증
    }

    // 비밀번호 복잡성 검증 실패 테스트
    @Test
    void testChangePasswordWithWeakPassword() {
        RequestChangePassword form = new RequestChangePassword();
        form.setToken("validToken");
        form.setPassword("weakpassword"); // 복잡성 기준을 만족하지 않는 비밀번호

        // 모킹: tempTokenService
        when(tempTokenService   .get("validToken")).thenReturn(tempToken);

        // 예외 발생 확인
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            memberUpdateService.changePassword(form);
        });

        assertEquals("Complexity.requestJoin.password", exception.getMessage()); // 메시지 검증
    }
}