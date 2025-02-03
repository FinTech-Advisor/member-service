package org.advisor.member.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.advisor.global.entities.BaseEntity;
import org.advisor.member.constants.Authority;
import org.advisor.member.constants.Status;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Member extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성
    private Long seq; // 회원 번호, 기본 키

    @Column(length = 65, nullable = false, unique = true)
    private String id; // 사용자 ID (기본 키가 아님)

    @Column(length = 65, nullable = false, unique = true)
    private String email; // 이메일

    @Column(length = 65)
    private String password;

    @Column(length = 65)
    private String confirmPassword;

    @Column(length = 40, nullable = false)
    private String name;

    private String mobile;

    private boolean requiredTerms1;
    private boolean requiredTerms2;
    private boolean requiredTerms3;

    @Column
    private String optionalTerms; // 선택 약관

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "member")
    private List<Authorities> authorities;

    private LocalDateTime credentialChangedAt; // 비밀번호 변경 일시

    private Status status;

    public void updateProfile(String name, String email, String mobile, String password, String confirmPassword) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public List<Authority> get_authorities() {
        return authorities == null || authorities.isEmpty() ? List.of()
                : authorities.stream().map(Authorities::getAuthority).toList();
    }
}