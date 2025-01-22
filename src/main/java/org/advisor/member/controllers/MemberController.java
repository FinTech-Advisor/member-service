package org.advisor.member.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.advisor.global.libs.Utils;
import org.advisor.global.rests.JSONData;
import org.advisor.member.MemberInfo;
import org.advisor.member.entities.Member;
import org.advisor.member.services.MemberUpdateService;
import org.advisor.member.validators.JoinValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 인증/인가 API")
@RestController
@RequiredArgsConstructor
public class MemberController {

    @Value("${front.domain}")
    private String frontDomain;

    private final Utils utils;
    private final MemberUpdateService updateService;
    private final JoinValidator joinValidator;


    @GetMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public String join(@RequestBody @Valid RequestJoin form, Errors errors) {
        return null;
    }
    @PostMapping("/join_ps")
    @ResponseStatus(HttpStatus.CREATED)
    public String join_ps(@RequestBody @Valid RequestJoin form, Errors errors) {
        return null;
    }

    @GetMapping("/agree")
    public String agree(RequestJoin form, Errors errors){
        return null;
    }

    @PostMapping("/logout")
    public String logout(){
        return null;
    }
    @GetMapping("/login")
    public JSONData login(@RequestBody @Valid RequestLogin form, Errors errors, HttpServletResponse response) {
        return null;
    }
    /**
     * 로그인 성공시 토큰 발급
     *
     * @param form
     * @param errors
     */
    @PostMapping("/login_ps")
    public JSONData login_ps(@RequestBody @Valid RequestLogin form, Errors errors, HttpServletResponse response) {
        return null;
    }
    @GetMapping("/findId/{email}")
    public JSONData findId(@PathVariable("email") String email, @Valid Member form){
        return null;
    }
    @PostMapping("/findId_ps/{email}")
    public JSONData findId_ps(@PathVariable("email") String email, @Valid Member form){
        return null;
    }
    @GetMapping("/findPw/{email}")
    public JSONData findPw(@PathVariable("email") String email, @Valid Member form){
        return null;
    }
    @PostMapping("/findPw_ps/{email}")
    public JSONData findPw_ps(@PathVariable("email") String email, @Valid Member form){
        return null;
    }
    /**
     * 로그인한 회원정보 조회
     * @return
     */
    @GetMapping("/")
    public JSONData info(@AuthenticationPrincipal MemberInfo memberInfo) {

        return null;
    }
}
