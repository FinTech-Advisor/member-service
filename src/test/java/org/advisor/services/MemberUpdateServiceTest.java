package org.advisor.services;

import org.advisor.global.libs.Utils;
import org.advisor.member.constants.Status;
import org.advisor.member.entities.Member;
import org.advisor.member.repositories.AuthoritiesRepository;
import org.advisor.member.repositories.MemberRepository;
import org.advisor.member.services.MemberUpdateService;
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

    @InjectMocks
    private MemberUpdateService memberUpdateService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member();
        testMember.setId("member123");
        testMember.setName("John Doe");
        testMember.setEmail("johndoe@example.com");
        testMember.setPhone("010-1234-5678");
        testMember.setPassword("password123");
        testMember.setConfirmPassword("password123");
        testMember.setRequiredTerms1(true);
        testMember.setRequiredTerms2(true);
        testMember.setRequiredTerms3(true);
        testMember.setOptionalTerms("");
        testMember.setCredentialChangedAt(LocalDateTime.now());
        testMember.setStatus(Status.ACTIVE);
    }

    @Test
    void testGetAllMembers() {
        when(memberRepository.findAll()).thenReturn(List.of(testMember));

        var members = memberUpdateService.getAllMembers();

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(testMember.getId(), members.get(0).getId());
    }

    @Test
    void testUpdateMemberProfile_Success() {
        String updatedName = "Jane Doe";
        String updatedEmail = "janedoe@example.com";
        String updatedPhone = "010-2345-6789";
        String updatedPassword = "newpassword123";

        when(passwordEncoder.encode(updatedPassword)).thenReturn("hashedPassword123");
        when(memberRepository.findById("member123")).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member updatedMember = memberUpdateService.updateMemberProfile(
                "member123", updatedName, updatedEmail, updatedPhone, updatedPassword);

        assertNotNull(updatedMember);
        assertEquals(updatedName, updatedMember.getName());
        assertEquals(updatedEmail, updatedMember.getEmail());
        assertEquals(updatedPhone, updatedMember.getPhone());
        assertNotEquals(updatedPassword, updatedMember.getPassword());
        assertEquals("hashedPassword123", updatedMember.getPassword());
    }

    @Test
    void testUpdateMemberRoles_Failure_InvalidRole() {
        List<String> newRoles = List.of("INVALID_ROLE");

        when(memberRepository.findById("member123")).thenReturn(Optional.of(testMember));
        when(authoritiesRepository.findAllById(testMember.getId())).thenReturn(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberUpdateService.updateMemberRoles("member123", newRoles);
        });

        assertTrue(exception.getMessage().contains("Invalid role"));
    }

    @Test
    void testChangeMemberPassword_Failure_ShortPassword() {
        String shortPassword = "short";

        when(memberRepository.findById("member123")).thenReturn(Optional.of(testMember));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberUpdateService.changeMemberPassword("member123", shortPassword);
        });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Password must be at least 8 characters"));
    }

    @Test
    void testUpdateMemberProfile_Failure_MemberNotFound() {
        String invalidMemberId = "nonexistentMember";
        String updatedName = "Jane Doe";
        String updatedEmail = "janedoe@example.com";
        String updatedPhone = "010-2345-6789";
        String updatedPassword = "newpassword123";

        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberUpdateService.updateMemberProfile(
                    invalidMemberId, updatedName, updatedEmail, updatedPhone, updatedPassword);
        });

        assertTrue(exception.getMessage().contains("Member not found with id"));
    }

    @Test
    void testDeleteMember() {
        when(memberRepository.findById("member123")).thenReturn(Optional.of(testMember));

        memberUpdateService.deleteMember("member123");

        verify(memberRepository, times(1)).delete(testMember);
    }

    @Test
    void testChangeMemberPassword() {
        String newPassword = "newPassword123";

        when(memberRepository.findById("member123")).thenReturn(Optional.of(testMember));
        when(passwordEncoder.encode(newPassword)).thenReturn("hashedNewPassword123");
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        memberUpdateService.changeMemberPassword("member123", newPassword);

        assertNotEquals(newPassword, testMember.getPassword());
        assertEquals("hashedNewPassword123", testMember.getPassword());
    }

    @Test
    void testUpdateMemberRoles() {
        List<String> newRoles = List.of("ADMIN", "USER");

        when(memberRepository.findById("member123")).thenReturn(Optional.of(testMember));
        when(authoritiesRepository.findAllById(testMember.getId())).thenReturn(List.of());

        Member updatedMember = memberUpdateService.updateMemberRoles("member123", newRoles);

        assertNotNull(updatedMember);
        verify(authoritiesRepository, times(1)).deleteAll(testMember.getId());
        verify(authoritiesRepository, times(1)).saveAll(any());
    }
}