package org.advisor.member.libs;

import jakarta.servlet.http.HttpSession;
import org.advisor.member.MemberInfo;
import org.advisor.member.constants.Authority;
import org.advisor.member.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class MemberUtil {

    @Autowired
    private HttpSession session;

    public boolean isLogin() {
        return getMember() != null;
    }

    /**
     * 관리자 여부
     *  권한 - MANAGER, ADMIN
     * @return
     */
    public boolean isAdmin() {
        return isLogin() &&
                getMember().getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority() == Authority.ADMIN);
    }

    /**
     * 로그인 한 회원의 정보 조회
     *
     * @return
     */
    public Member getMember() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof MemberInfo memberInfo) {
            Member member = (Member)session.getAttribute("member");
            if (member == null) {
                member = memberInfo.getMember();
                session.setAttribute("member", member);

                return member;
            } else {
                return member;
            }
        }

        return null;
    }
}
