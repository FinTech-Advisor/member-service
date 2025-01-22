package org.advisor.member.controllers;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.advisor.member.constants.Gender;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RequestJoin {
    @Id
    @NotBlank
    private String id;
    @Email
    @NotBlank
    private String email; // 이메일

    @NotBlank
    private String name; // 회원명

    @NotBlank
    @Size(min=8)
    private String password; // 비밀번호

    @NotBlank
    private String confirmPassword; // 비밀번호 확인

   @NotBlank
    private String phone;
    @NotNull
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate birthDt;  // 생년월일

    @NotNull
    private Gender gender; // 성별

    @NotBlank
    private String zipCode; // 우편번호

    @NotBlank
    private String address; // 주소
    private String addressSub; // 나머지 주소




}
