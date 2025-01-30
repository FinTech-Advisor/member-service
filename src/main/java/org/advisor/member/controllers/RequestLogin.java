package org.advisor.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestLogin {
    @NotBlank
    private String id;

    @NotBlank
    private String password;
}
