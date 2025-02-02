package org.advisor.member.services;

import lombok.RequiredArgsConstructor;
import org.advisor.member.constants.Authority;
import org.advisor.member.constants.Status;
import org.advisor.member.controllers.RequestJoin;
import org.advisor.member.entities.Authorities;
import org.advisor.member.entities.Member;
import org.advisor.member.repositories.AuthoritiesRepository;
import org.advisor.member.repositories.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Lazy
@Service
@RequiredArgsConstructor
@Transactional
public class MemberUpdateService {

    private final MemberRepository memberRepository;
    private final AuthoritiesRepository authoritiesRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    /**
     * 회원 목록 조회
     * @return 모든 회원 목록
     */
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 상세 조회 (회원 ID로 조회)
     * @param memberId 회원 ID
     * @return 해당 회원
     */
    public Member getMemberById(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));
    }

    /**
     * 회원 정보 수정
     * @param memberId 수정할 회원의 ID
     * @param name 수정할 이름
     * @param email 수정할 이메일
     * @param phone 수정할 전화번호
     * @param password 새 비밀번호
     * @return 수정된 회원
     */
    public Member updateMemberProfile(String memberId, String name, String email, String phone, String password) {
        Member member = getMemberById(memberId);

        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        member.setPassword(passwordEncoder.encode(password));

        return memberRepository.save(member);
    }

    /**
     * 회원 삭제
     * @param memberId 삭제할 회원 ID
     */
    public void deleteMember(String memberId) {
        Member member = getMemberById(memberId);

        // 권한 삭제 및 회원 삭제
        authoritiesRepository.deleteAllByMember_Id(memberId);
        memberRepository.delete(member);
    }

    /**
     * 회원 비밀번호 변경
     * @param memberId 회원 ID
     * @param newPassword 새 비밀번호
     * @return 변경된 회원
     */
    public Member changeMemberPassword(String memberId, String newPassword) {
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        Member member = getMemberById(memberId);
        member.setPassword(passwordEncoder.encode(newPassword));

        return memberRepository.save(member);
    }

    /**
     * 회원 권한 수정
     * @param memberId 회원 ID
     * @param newRoles 새로운 권한 목록
     * @return 수정된 회원
     */
    public Member updateMemberRoles(String memberId, List<String> newRoles) {
        Member member = getMemberById(memberId);

        // 권한 유효성 검증 및 업데이트
        if (newRoles != null && !newRoles.isEmpty()) {
            authoritiesRepository.deleteAllByMember_Id(memberId);

            List<Authorities> authorities = newRoles.stream()
                    .map(role -> {
                        if (!Authority.isValid(role)) {
                            throw new RuntimeException("Invalid role: " + role);
                        }
                        Authorities authority = new Authorities();
                        authority.setMember(member);
                        authority.setAuthority(Authority.valueOf(role));
                        return authority;
                    })
                    .toList();

            authoritiesRepository.saveAll(authorities);
        }

        return member;
    }

    /**
     * 회원 가입 처리
     * @param form 회원 가입 요청 데이터
     */
    public void process(RequestJoin form) {
        Member member = modelMapper.map(form, Member.class);

        if (form.getOptionalTerms() != null) {
            member.setOptionalTerms(String.join("||", form.getOptionalTerms()));
        }

        member.setPassword(passwordEncoder.encode(form.getPassword()));
        member.setCredentialChangedAt(LocalDateTime.now());

        Authorities defaultAuthority = new Authorities();
        defaultAuthority.setMember(member);
        defaultAuthority.setAuthority(Authority.USER);

        save(member, List.of(defaultAuthority));
    }

    /**
     * 회원 검색
     * @param name 이름
     * @param email 이메일
     * @return 검색된 회원 목록
     */
    public List<Member> searchMembers(String name, String email) {
        if (name != null && !name.isEmpty()) {
            return memberRepository.findByNameContaining(name);
        } else if (email != null && !email.isEmpty()) {
            return memberRepository.findByEmailContaining(email);
        } else {
            return memberRepository.findAll();
        }
    }

    /**
     * 회원 상태 변경
     * @param memberId 회원 ID
     * @param status 변경할 상태
     * @return 변경된 회원
     */
    public Member changeMemberStatus(String memberId, Status status) {
        Member member = getMemberById(memberId);

        member.setStatus(status);
        return memberRepository.save(member);
    }

    /**
     * 이메일 중복 확인
     * @param email 이메일
     * @return 중복 여부
     */
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    private void save(Member member, List<Authorities> authorities) {
        memberRepository.save(member);

        if (authorities != null) {
            authoritiesRepository.deleteAllByMember_Id(member.getId());
            authoritiesRepository.saveAll(authorities);
        }
    }
}
