package org.advisor.member.services;

import org.advisor.member.MemberInfo;
import org.advisor.member.constants.Authority;
import org.advisor.member.entities.Authorities;
import org.advisor.member.entities.Member;
import org.advisor.member.repositories.MemberRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class MemberInfoService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final JPAQueryFactory queryFactory;
    private final HttpServletRequest request;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Member member = memberRepository.findById(name).orElseThrow(() -> new UsernameNotFoundException(username));


        List<Authorities> items = member.getAuthorities();
        if (items == null) {
            Authorities auth = new Authorities();
            auth.setMember(member);
            auth.setAuthority(Authority.USER);
            items = List.of(auth);
        }


        List<SimpleGrantedAuthority> authorities = items.stream().map(a -> new SimpleGrantedAuthority(a.getAuthority().name())).toList();

        // 추가 정보 처리
        addInfo(member);

        return MemberInfo.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .member(member)
                .authorities(authorities)
                .build();
    }

    public Member get(String email) {
        MemberInfo memberInfo = (MemberInfo)loadUserByUsername(email);
        return memberInfo.getMember();
    }


    /**
     * 추가 정보 처리
     * @param member
     */
    public void addInfo(Member member) {

    }
}