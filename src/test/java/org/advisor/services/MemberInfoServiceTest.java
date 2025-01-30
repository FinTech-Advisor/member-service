package org.advisor.services;

import org.advisor.member.MemberInfo;
import org.advisor.member.constants.Authority;
import org.advisor.member.constants.Status;
import org.advisor.member.entities.Authorities;
import org.advisor.member.entities.Member;
import org.advisor.member.exceptions.ResourceNotFoundException;
import org.advisor.member.repositories.MemberRepository;
import org.advisor.member.services.MemberInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberInfoServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MemberInfoService memberInfoService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = createTestMember();
    }

    private Member createTestMember() {
        Member member = new Member();
        member.setId("member123");
        member.setEmail("johndoe@example.com");
        member.setPassword("password123");
        member.setStatus(Status.ACTIVE);

        Authorities authority = new Authorities();
        authority.setMember(member);
        authority.setAuthority(Authority.USER);

        member.setAuthorities(List.of(authority));
        return member;
    }

    private MemberInfo createTestMemberInfo() {
        return MemberInfo.builder()
                .email(testMember.getEmail())
                .password(testMember.getPassword())
                .authorities(testMember.getAuthorities().stream()
                        .map(auth -> new SimpleGrantedAuthority(auth.getAuthority().name()))
                        .collect(Collectors.toList()))
                .build();
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        // given
        when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(Optional.of(testMember));

        // when
        MemberInfo result = (MemberInfo) memberInfoService.loadUserByUsername(testMember.getEmail());

        // then
        assertNotNull(result);
        assertEquals(testMember.getEmail(), result.getEmail());
        assertEquals(testMember.getPassword(), result.getPassword());

        List<String> roles = result.getAuthorities().stream()
                .map(authority -> authority.getAuthority()) // 타입에 관계없이 안전하게 접근
                .collect(Collectors.toList());
        assertTrue(roles.contains("USER"));

    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        when(memberRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class,
                () -> memberInfoService.loadUserByUsername("invalid@example.com"));
    }

    @Test
    void shouldReturnAllMembers() {
        // given
        when(memberRepository.findAll()).thenReturn(List.of(testMember));
        when(modelMapper.map(testMember, MemberInfo.class)).thenReturn(createTestMemberInfo());

        // when
        List<MemberInfo> result = memberInfoService.getAllMembers();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMember.getEmail(), result.get(0).getEmail());
    }

    @Test
    void shouldReturnMemberByIdSuccessfully() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(modelMapper.map(testMember, MemberInfo.class)).thenReturn(createTestMemberInfo());

        // when
        MemberInfo result = memberInfoService.getMemberBySeq(1L);

        // then
        assertNotNull(result);
        assertEquals(testMember.getEmail(), result.getEmail());
    }

    @Test
    void shouldReturnNullWhenMemberByIdNotFound() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        MemberInfo result = memberInfoService.getMemberBySeq(1L);

        // then
        assertNull(result);
    }

    @Test
    void shouldUpdateMemberSuccessfully() {
        // given
        MemberInfo updatedInfo = MemberInfo.builder()
                .email("newemail@example.com")
                .password("newpassword123")
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(modelMapper.map(testMember, MemberInfo.class)).thenReturn(updatedInfo);

        // when
        MemberInfo result = memberInfoService.updateMember(1L, updatedInfo);

        // then
        assertNotNull(result);
        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("newpassword123", result.getPassword());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentMember() {
        // given
        MemberInfo updatedInfo = MemberInfo.builder()
                .email("newemail@example.com")
                .password("newpassword123")
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class,
                () -> memberInfoService.updateMember(1L, updatedInfo));
    }

    @Test
    void shouldKickOutMemberSuccessfully() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // when
        boolean result = memberInfoService.kickOutMember(1L, Status.INACTIVE);

        // then
        assertTrue(result);
        assertEquals(Status.INACTIVE, testMember.getStatus());
        verify(memberRepository, times(1)).save(testMember);
    }

    @Test
    void shouldNotKickOutNonExistentMember() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        boolean result = memberInfoService.kickOutMember(1L, Status.INACTIVE);

        // then
        assertFalse(result);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void shouldReturnMemberInfoByEmailSuccessfully() {
        // given
        when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(Optional.of(testMember));

        // when
        MemberInfo result = memberInfoService.getMemberInfoByEmail(testMember.getEmail());

        // then
        assertNotNull(result);
        assertEquals(testMember.getEmail(), result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenMemberInfoByEmailNotFound() {
        // given
        when(memberRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class,
                () -> memberInfoService.getMemberInfoByEmail("invalid@example.com"));
    }
}
