package org.advisor.member.validators;

import org.advisor.global.exceptions.BadRequestException;
import org.advisor.global.libs.Utils;
import org.advisor.member.entities.Member;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
public class MemberUpdateValidator implements Validator {

    private final Utils utils;

    public MemberUpdateValidator(Utils utils) {
        this.utils = utils;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        // Member 클래스에 대해서만 검증을 수행
        return Member.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Member member = (Member) target;

        // 이름 검증
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            errors.rejectValue("name", "name.empty", "이름은 필수 항목입니다.");
        }

        // 이메일 검증 (이메일 패턴)
        if (member.getEmail() == null || member.getEmail().trim().isEmpty()) {
            errors.rejectValue("email", "email.empty", "이메일은 필수 항목입니다.");
        } else if (!isValidEmail(member.getEmail())) {
            errors.rejectValue("email", "email.invalid", "유효한 이메일 주소를 입력해 주세요.");
        }

        // 전화번호 검증 (전화번호 패턴)
        if (member.getPhone() != null && !member.getPhone().trim().isEmpty()) {
            if (!isValidPhoneNumber(member.getPhone())) {
                errors.rejectValue("phone", "phone.invalid", "유효한 전화번호를 입력해 주세요.");
            }
        }

        // 비밀번호 검증 (비밀번호가 있을 경우)
        if (member.getPassword() != null && !isValidPassword(member.getPassword())) {
            errors.rejectValue("password", "password.invalid", "비밀번호는 최소 8자 이상, 대소문자 및 특수문자를 포함해야 합니다.");
        }

        // 유효성 검사 후 에러가 있으면 예외를 던짐
        if (errors.hasErrors()) {
            throw new BadRequestException(utils.getErrorMessages(errors));
        }
    }

    /**
     * 이메일 유효성 검사
     */
    private boolean isValidEmail(String email) {
        // 이메일 패턴 정규식
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    /**
     * 전화번호 유효성 검사 (예시: 한국 전화번호 형식)
     */
    private boolean isValidPhoneNumber(String phone) {
        // 전화번호 패턴 정규식 (예: 010-1234-5678, 01012345678 형태)
        String phoneRegex = "^(\\d{3})-?(\\d{3,4})-?(\\d{4})$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone).matches();
    }

    /**
     * 비밀번호 유효성 검사
     * 최소 8자 이상, 대소문자, 특수문자 포함
     */
    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        return Pattern.compile(passwordRegex).matcher(password).matches();
    }
}
