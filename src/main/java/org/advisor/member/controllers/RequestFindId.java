
package org.advisor.member.controllers;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestFindId {
    @NotBlank
    private String name; // 회원명
    @NotBlank
    private String email; // 회원명
    @NotBlank
    private String mobile; // 휴대전화번호
}
