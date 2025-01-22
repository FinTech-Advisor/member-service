package org.advisor.mypage.controllers;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.advisor.member.constants.Authority;
import org.advisor.member.constants.Gender;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class RequestProfile {

    @Id
    @NotBlank
    private String id;

    private String email;

    @NotBlank
    private String name; // 회원명


    //@Size(min=8)
    private String password;
    private String confirmPassword;

    @NotNull
    private Gender gender; // 성별

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDt; // 생년월일

    @NotBlank
    private String zipCode;

    @NotBlank
    private String address;
    private String addressSub;

    private List<String> optionalTerms; // 추가 선택 약관

    private List<Authority> authorities;




}
