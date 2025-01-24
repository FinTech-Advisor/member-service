package org.advisor.member.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.advisor.global.exceptions.BadRequestException;
import org.advisor.global.libs.Utils;
import org.advisor.global.rests.JSONData;
import org.advisor.member.MemberInfo;
import org.advisor.member.entities.Member;
import org.advisor.member.jwt.TokenService;
import org.advisor.member.repositories.MemberRepository;
import org.advisor.member.services.MemberUpdateService;
import org.advisor.member.validators.JoinValidator;
import org.advisor.member.validators.LoginValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Tag(name = "Member", description = "회원 인증/인가 API")
@RestController
@RequiredArgsConstructor
public class MemberController {

    @Value("${front.domain}")
    private String frontDomain;
    private final LoginValidator loginValidator;
    private final TokenService tokenService;
    private final Utils utils;
    private final MemberUpdateService updateService;
    private final JoinValidator joinValidator;
    private final MemberRepository memberRepository;
    private JavaMailSender mailSender;

    @Operation(summary = "회원 가입", method = "POST")
    @ApiResponse(responseCode = "201", description = "회원 가입 성공시 201")
    @Parameters({
            @Parameter(name = "email", required = true, description = "이메일"),
            @Parameter(name = "password", required = true, description = "비밀번호"),
            @Parameter(name = "confirmPassword", required = true, description = "비밀번호 확인"),
            @Parameter(name = "name", required = true, description = "사용자명"),
            @Parameter(name = "phone", description = "휴대전화번호, 형식 검증 있음"),
            @Parameter(name = "birthDt", required = true, description = "생년월일"),
            @Parameter(name = "gender", required = true, description = "성별"),
            @Parameter(name = "zipCode", description = "우편번호"),
            @Parameter(name = "address", description = "주소"),
            @Parameter(name = "addressSub", description = "나머지 주소"),
            @Parameter(name = "requiredTerms1", required = true, description = "필수 약관 동의 여부"),
            @Parameter(name = "requiredTerms2", required = true, description = "필수 약관 동의 여부"),
            @Parameter(name = "requiredTerms3", required = true, description = "필수 약관 동의 여부"),
            @Parameter(name = "optionalTerms", required = true, description = "선택 약관 동의")
    })
    @PostMapping("/join")
    public JSONData join(@RequestBody @Valid RequestJoin form, Errors errors) {

        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }

        String token = tokenService.create(form.getEmail(), form.getPassword());

        return new JSONData(token);
    }



        @PostMapping("/logout")
        public String logout(HttpSession session){
            // 세션을 무효화하여 로그아웃 처리
            session.invalidate();
            return "redirect:/member/login";
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

        String email = form.getId();
        String token = tokenService.create(email);



        if (StringUtils.hasText(frontDomain)) {
            String[] domains = frontDomain.split(",");
            for (String domain : domains) {


                response.setHeader("Set-Cookie", String.format("token=%s; Path=/; Domain=%s; Secure; HttpOnly; SameSite=None", token, domain)); // SameSite: None - 다른 서버에서도 쿠키 설정 가능, Https는 필수
            }
        }

        return new JSONData(token);
    }
    @PostMapping("/findId/{email}")
    public JSONData findId(@PathVariable("email") String email, @Valid Member form){
        // 이메일에 해당하는 회원을 찾음
        Optional<Member> memberOpt = memberRepository.findByEmail(email);

        if (memberOpt.isPresent()) {
            String memberId = memberOpt.get().getId();
            return new JSONData("아이디 찾기 성공", memberId); // 아이디를 반환
        } else {
            return new JSONData("이메일에 해당하는 아이디가 없습니다.", null); // 아이디가 없을 경우
        }
    }

    @PostMapping("/findPw/{email}")
    public JSONData findPw(@PathVariable("email") String email, @Valid Member form){
        // 이메일에 해당하는 회원을 찾음
        Optional<Member> memberOpt = memberRepository.findByEmail(email);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            // 비밀번호 재설정을 위한 인증 토큰 생성 (임시 토큰)
            String resetToken = UUID.randomUUID().toString();

            // 토큰을 DB에 저장하거나, 세션에 저장하는 등의 로직을 추가할 수 있음.
            // 예를 들어: memberService.savePasswordResetToken(member, resetToken);

            // 비밀번호 재설정을 위한 링크를 생성 (예시 URL: /reset-password?token=토큰)
            String resetLink = "." + resetToken;

            // 이메일로 비밀번호 재설정 링크 전송
            sendPasswordResetEmail(email, resetLink);

            return new JSONData("비밀번호 재설정 링크가 이메일로 전송되었습니다.", null);
        } else {
            return new JSONData("이메일에 해당하는 회원이 존재하지 않습니다.", null);
        }

    }


    /**
     * 로그인한 회원정보 조회
     * @return
     */
    @GetMapping("/")
    public JSONData info(@AuthenticationPrincipal MemberInfo memberInfo) {
        Member member = memberInfo.getMember();

        return new JSONData(member);
    }
    // 비밀번호 재설정 이메일 전송
    private void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("비밀번호 재설정 요청");
        message.setText("비밀번호 재설정을 원하시면 아래 링크를 클릭하세요:\n" + resetLink);

    }
}
