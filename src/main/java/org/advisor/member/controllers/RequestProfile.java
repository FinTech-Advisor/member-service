package org.advisor.member.controllers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestProfile {

    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    private String phone;

    // 추가적인 필드들 (예: 주소, 생일 등) 필요하면 여기에 추가
}
