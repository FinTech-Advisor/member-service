package org.advisor.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequstFindPw {
    @NotBlank
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String email;

}
