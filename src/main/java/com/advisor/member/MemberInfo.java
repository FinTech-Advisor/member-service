package com.advisor.member;

import com.advisor.member.entities.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@Builder
@ToString
public class MemberInfo implements UserDetails {

    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        LocalDateTime credentialChangedAt = member.getCredentialChangedAt();
        return credentialChangedAt != null &&
                credentialChangedAt.isAfter(LocalDateTime.now().minusMonths(1L));
    }

    @Override
    public boolean isEnabled() { // 회원 탈퇴 여부
        return member.getDeletedAt() == null;
    }
}