package com.advisor.member.exceptions;


import com.advisor.global.exceptions.CommonException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends CommonException {
    public MemberNotFoundException() {
        super("NotFound.member", HttpStatus.NOT_FOUND);
        setErrorCode(true);
    }
}
