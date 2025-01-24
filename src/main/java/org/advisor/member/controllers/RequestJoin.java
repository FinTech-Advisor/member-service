package org.advisor.member.controllers;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.advisor.member.constants.Gender;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class RequestJoin {
    @Id
    @NotBlank(message = "아이디는 필수입니다.")
    @Column(length = 45, nullable = false, unique = true)
    private String id; // 회원 아이디

    @Email(message = "유효한 이메일 주소를 입력하세요.")
    @NotBlank(message = "이메일은 필수입니다.")
    @Column(length = 65, nullable = false, unique = true)
    private String email; // 이메일

    @NotBlank(message = "이름은 필수입니다.")
    @Column(length = 40, nullable = false)
    private String name; // 회원명

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password; // 비밀번호

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword; // 비밀번호 확인

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 10자리 또는 11자리 숫자로 입력해주세요.")
    private String phone; // 전화번호

    @NotNull(message = "생년월일은 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDt; // 생년월일

    @NotNull(message = "성별은 필수입니다.")
    @Enumerated(EnumType.STRING)  // Gender enum을 사용
    private Gender gender; // 성별

    @NotBlank(message = "우편번호는 필수입니다.")
    private String zipCode; // 우편번호

    @NotBlank(message = "주소는 필수입니다.")
    private String address; // 주소

    private String addressSub; // 나머지 주소 (선택적)

    @AssertTrue(message = "필수 약관1에 동의해야 합니다.")
    private boolean requiredTerms1; // 필수 약관1 동의 여부

    @AssertTrue(message = "필수 약관2에 동의해야 합니다.")
    private boolean requiredTerms2; // 필수 약관2 동의 여부

    @AssertTrue(message = "필수 약관3에 동의해야 합니다.")
    private boolean requiredTerms3; // 필수 약관3 동의 여부

    @ElementCollection
    private List<String> optionalTerms; // 선택 약관

}
