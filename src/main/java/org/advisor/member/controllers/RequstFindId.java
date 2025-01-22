package org.advisor.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequstFindId {
    @NotBlank
    private String id;
    @NotBlank
    private String email;

}
