package org.advisor.member.services;

import lombok.RequiredArgsConstructor;
import org.advisor.member.constants.Authority;
import org.advisor.member.constants.Status;
import org.advisor.member.controllers.RequestJoin;
import org.advisor.member.entities.Authorities;
import org.advisor.member.entities.Member;
import org.advisor.member.entities.QAuthorities;
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

@Lazy // 지연로딩 - 최초로 빈을 사용할 때 생성
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
        return memberRepository.findAll(); // 모든 회원 조회
    }

    /**
     * 회원 상세 조회 (회원 ID 또는 seq로 조회)
     * @param memberId 회원 ID
     * @return 해당 회원
     */
    public Member getMemberById(String memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        return member.orElse(null); // 회원이 없으면 null 반환
    }

    /**
     * 회원 정보 수정
     * @param memberId 수정할 회원의 ID
     * @param name 수정할 이름
     * @param email 수정할 이메일
     * @param phone 수정할 전화번호
     * @return 수정된 회원
     */
    public Member updateMemberProfile(String memberId, String name, String email, String phone, String password) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        // 정보 수정
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        member.setPassword(passwordEncoder.encode(password)); // 비밀번호는 해시화

        // 수정된 회원 저장
        return memberRepository.save(member);
    }

    /**
     * 회원 강퇴 (회원 삭제)
     * @param memberId 탈퇴할 회원의 ID
     */
    public void deleteMember(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        // 회원 권한 삭제
        QAuthorities qAuthorities = QAuthorities.authorities;
        List<Authorities> authorities = (List<Authorities>) authoritiesRepository.findAll(qAuthorities.member.eq(member));
        if (authorities != null && !authorities.isEmpty()) {
            authoritiesRepository.deleteAll(authorities);
            authoritiesRepository.flush();
        }

        // 회원 삭제
        memberRepository.delete(member);
    }

    /**
     * 회원 정보 추가 또는 수정 처리
     * @param member 새로운 회원
     * @param authorities 권한 목록
     */
    private void save(Member member, List<Authorities> authorities) {
        memberRepository.saveAndFlush(member);

        // 회원 권한 업데이트 처리
        if (authorities != null) {
            /**
             * 기존 권한을 삭제하고 다시 등록
             */
            QAuthorities qAuthorities = QAuthorities.authorities;
            List<Authorities> items = (List<Authorities>) authoritiesRepository.findAll(qAuthorities.member.eq(member));
            if (items != null) {
                authoritiesRepository.deleteAll(items);
                authoritiesRepository.flush();
            }

            authoritiesRepository.saveAllAndFlush(authorities);
        }
    }

    /**
     * 회원 비밀번호 변경
     * @param memberId 회원 ID
     * @param newPassword 새 비밀번호
     */
    public void changeMemberPassword(String memberId, String newPassword) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        // 비밀번호 해시화
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.setPassword(encodedPassword);

        memberRepository.save(member); // 비밀번호 변경 후 저장
    }

    /**
     * 회원 권한 수정
     * @param memberId 회원 ID
     * @param newRoles 새로운 권한 리스트
     * @return 수정된 회원
     */
    public Member updateMemberRoles(String memberId, List<String> newRoles) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        // 기존 권한 삭제
        QAuthorities qAuthorities = QAuthorities.authorities;
        List<Authorities> existingAuthorities = (List<Authorities>) authoritiesRepository.findAll(qAuthorities.member.eq(member));

        if (existingAuthorities != null && !existingAuthorities.isEmpty()) {
            authoritiesRepository.deleteAll(existingAuthorities);
            authoritiesRepository.flush();
        }

        // 새 권한 추가
        if (newRoles != null && !newRoles.isEmpty()) {
            for (String role : newRoles) {
                Authorities authority = new Authorities();
                authority.setMember(member);
                authority.setAuthority(Authority.valueOf(role)); // 권한을 Authority enum으로 변환
                authoritiesRepository.save(authority); // 새 권한 저장
            }
        }

        return member;
    }

    /**
     * 커맨드 객체의 타입에 따라서 RequestJoin이면 회원 가입 처리
     *                      RequestProfile이면 회원정보 수정 처리
     * @param form
     */
    public void process(RequestJoin form) {
        // 커맨드 객체 -> 엔티티 객체 데이터 옮기기
        Member member = modelMapper.map(form, Member.class);

        // 선택 약관 -> 약관 항목1||약관 항목2||...
        List<String> optionalTerms = form.getOptionalTerms();
        if (optionalTerms != null) {
            member.setOptionalTerms(String.join("||", optionalTerms));
        }

        // 비밀번호 해시화 - BCrypt
        String hash = passwordEncoder.encode(form.getPassword());
        member.setPassword(hash);
        member.setCredentialChangedAt(LocalDateTime.now());

        // 회원 권한
        Authorities auth = new Authorities();
        auth.setMember(member);
        auth.setAuthority(Authority.USER);  // 회원 권한이 없는 경우 - 회원 가입시, 기본 권한 USER

        save(member, List.of(auth)); // 회원 저장 처리
    }
    /**
     * 회원 검색 (이름이나 이메일을 기반으로 검색)
     * @param name 회원 이름
     * @param email 회원 이메일
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
     * 회원 상태 변경 (활성화/비활성화)
     * @param memberId 회원 ID
     * @param status 변경할 상태
     * @return 수정된 회원
     */
    public Member changeMemberStatus(String memberId, Status status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + memberId));

        member.setStatus(status);
        return memberRepository.save(member);
    }
    /**
     * 이메일 중복 확인
     * @param email 확인할 이메일
     * @return 이메일이 중복되는지 여부
     */
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }
}
