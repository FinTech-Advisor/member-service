
package org.advisor.member.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.advisor.global.exceptions.BadRequestException;
import org.advisor.global.libs.Utils;
import org.advisor.global.rests.JSONData;
import org.advisor.member.MemberInfo;
import org.advisor.member.jwt.TokenService;
import org.advisor.member.services.MemberUpdateService;
import org.advisor.member.validators.JoinValidator;
import org.advisor.member.validators.LoginValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
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
    private final TokenService tokenService;
    private final LoginValidator loginValidator;

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public JSONData join(@RequestBody @Valid RequestJoin form, Errors errors) {
        joinValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        updateService.process(form);
        return new JSONData();
    }

    /**
     * 로그인 성공시 토큰 발급
     *
     * @param form
     * @param errors
     */
    @PostMapping("/login")
    public JSONData login(@RequestBody @Valid RequestLogin form, Errors errors, HttpServletResponse response) {

        loginValidator.validate(form, errors);

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        String id = form.getId();
        String token = tokenService.create(id);



        if (StringUtils.hasText(frontDomain)) {
            String[] domains = frontDomain.split(",");
            for (String domain : domains) {


                response.setHeader("Set-Cookie", String.format("token=%s; Path=/; Domain=%s; Secure; HttpOnly; SameSite=None", token, domain)); // SameSite: None - 다른 서버에서도 쿠키 설정 가능, Https는 필수
            }
        }

        return new JSONData(token);
    }

    /**
     * 로그인한 회원정보 조회
     * @return
     */
    @GetMapping("/")
    public JSONData info(@AuthenticationPrincipal MemberInfo memberInfo) {

        return new JSONData(memberInfo.getMember());
    }
}
