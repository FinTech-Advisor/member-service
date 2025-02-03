package org.advisor.member.services;

import lombok.RequiredArgsConstructor;
import org.advisor.global.exceptions.BadRequestException;
import org.advisor.global.libs.Utils;
import org.advisor.global.validators.PasswordValidator;
import org.advisor.member.constants.Authority;
import org.advisor.member.constants.Status;
import org.advisor.member.constants.TockenAction;
import org.advisor.member.controllers.RequestChangePassword;
import org.advisor.member.controllers.RequestFindId;
import org.advisor.member.controllers.RequestFindPassword;
import org.advisor.member.controllers.RequestJoin;
import org.advisor.member.entities.Authorities;
import org.advisor.member.entities.Member;
import org.advisor.member.entities.TempToken;
import org.advisor.member.exceptions.MemberNotFoundException;
import org.advisor.member.exceptions.TempTokenNotFoundException;
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
public class MemberUpdateService implements PasswordValidator {
    private final EmailService emailService;
    private final MemberRepository memberRepository;
    private final AuthoritiesRepository authoritiesRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TempTokenService tempTokenService;
    private final Utils utils;

    /**
     * 회원 목록 조회
     *
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
        member.setMobile(phone);
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
     * 회원이 입력한 회원명 + 휴대전화번호로 회원을 찾고
     * 가입한 이메일로 비번 변경 가능한 임시 토큰을 발급하고 메일을 전송
     *
     * @param form
     */
    public void issueToken(RequestFindPassword form) {
        String name = form.getName();
        String mobile = form.getMobile();

        Member member = memberRepository.findByNameAndMobile(name, mobile).orElseThrow(MemberNotFoundException::new);
        String email = member.getEmail();

        TempToken token = tempTokenService.issue(email, TockenAction.PASSWORD_CHANGE, form.getOrigin()); // 토큰 발급
        tempTokenService.sendEmail(token.getToken()); // 이메일 전송

    }

    /**
     * 비밀번호 변경
     *
     * @param form
     */
    public void changePassword(RequestChangePassword form) {
        String token = form.getToken();
        String password = form.getPassword();

        TempToken tempToken = tempTokenService.get(token);
        if (tempToken.getAction() != TockenAction.PASSWORD_CHANGE) {
            throw new TempTokenNotFoundException();
        }

        // 비밀번호 자리수 검증
        if (password.length() < 8) {
            throw new BadRequestException(utils.getMessage("Size.requestJoin.password"));
        }

        // 비밀번호 복잡성 검증
        if (!alphaCheck(password, false) || !numberCheck(password) || !specialCharsCheck(password)) {
            throw new BadRequestException(utils.getMessage("Complexity.requestJoin.password"));
        }

        Member member = tempToken.getMember();

        String hash = passwordEncoder.encode(password);
        member.setPassword(hash);
        member.setCredentialChangedAt(LocalDateTime.now());
        memberRepository.saveAndFlush(member);
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
    public void findId(RequestFindId form) {
        // ID 찾기 로직 구현
        String email = form.getEmail();
        Optional<Member> memberOptional = memberRepository.findByEmail(email);

        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            String loginId = member.getId();
            emailService.sendIdEmail(email, loginId); // 이메일 전송 서비스 호출
        } else {
            throw new BadRequestException("등록되지 않은 이메일입니다.");
        }
    }

    /**
     * 회원 검색
     *
     * @param name  이름
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
