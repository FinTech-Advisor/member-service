package org.advisor.member.services;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.advisor.member.MemberInfo;
import org.advisor.member.constants.Authority;
import org.advisor.member.constants.Status;
import org.advisor.member.entities.Authorities;
import org.advisor.member.entities.Member;
import org.advisor.member.exceptions.ResourceNotFoundException;
import org.advisor.member.repositories.MemberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Component
@Lazy
@Service
@RequiredArgsConstructor
public class MemberInfoService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final JPAQueryFactory queryFactory;
    private final HttpServletRequest request;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(id).orElseThrow(() -> new UsernameNotFoundException(id));

        List<Authorities> items = member.getAuthorities();
        if (items == null) {
            Authorities auth = new Authorities();
            auth.setMember(member);
            auth.setAuthority(Authority.USER);
            items = List.of(auth);
        }

        List<SimpleGrantedAuthority> authorities = items.stream()
                .map(a -> new SimpleGrantedAuthority(a.getAuthority().name()))
                .collect(Collectors.toList());

        addInfo(member);

        return MemberInfo.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .member(member)
                .authorities(authorities)
                .build();
    }

    public Member get(String id) {
        MemberInfo memberInfo = (MemberInfo) loadUserByUsername(id);
        return memberInfo.getMember();
    }

    /**
     * 추가 정보 처리
     * @param member
     */
    public void addInfo(Member member) {
        // 추가 정보 처리 (예: 세션 정보 처리, 사용자 로깅 등)
    }

    // =======================================================================
    // MemberAdminController에서 필요한 서비스 메서드 구현
    // =======================================================================

    /**
     * 모든 회원 목록 조회
     * @return 회원 목록
     */
    public List<MemberInfo> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(member -> modelMapper.map(member, MemberInfo.class))
                .collect(Collectors.toList());
    }

    /**
     * 회원 ID로 회원 상세 조회
     * @param seq 회원 ID
     * @return 회원 정보
     */
    public MemberInfo getMemberBySeq(Long seq) {
        Optional<Member> memberOptional = memberRepository.findById(seq);
        return memberOptional.map(member -> modelMapper.map(member, MemberInfo.class))
                .orElse(null);
    }

    /**
     * 회원 정보 수정
     * @param seq 회원 ID
     * @param memberInfo 수정할 회원 정보
     * @return 수정된 회원 정보
     */
    public MemberInfo updateMember(Long seq, MemberInfo memberInfo) {
        Optional<Member> memberOptional = memberRepository.findById(seq);
        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            // 수정된 필드를 업데이트
            member.setEmail(memberInfo.getEmail());
            member.setPassword(memberInfo.getPassword());
            // 예: 권한 수정, 기타 정보 업데이트 등
            memberRepository.save(member);
            return modelMapper.map(member, MemberInfo.class);
        } else {
            throw new ResourceNotFoundException("Member not found with id: " + seq);
        }
    }

    /**
     * 회원 강퇴
     * @param seq 회원 ID
     * @return 성공 여부
     */
    public boolean kickOutMember(Long seq, Status status) {
        Optional<Member> memberOptional = memberRepository.findById(seq);
        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            // 강퇴 처리 (예: 삭제, 상태 변경 등)
            // memberRepository.delete(member); // 삭제 예시
            member.setStatus(status);// 활성 상태 변경 예시
            memberRepository.save(member);
            return true;
        }
        return false;
    }

    // =======================================================================
    // MemberInfoService에서 JWT 인증 후 회원 정보를 로드하는 메서드
    // =======================================================================
    public MemberInfo getMemberInfoByEmail(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Member not found with email: " + email));
        List<SimpleGrantedAuthority> authorities = member.getAuthorities().stream()
                .map(auth -> new SimpleGrantedAuthority(auth.getAuthority().name()))
                .collect(Collectors.toList());

        return MemberInfo.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .authorities(authorities)
                .build();
    }
}
