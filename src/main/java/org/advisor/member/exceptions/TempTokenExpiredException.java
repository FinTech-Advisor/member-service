
package org.advisor.member.exceptions;


import org.advisor.global.exceptions.CommonException;
import org.springframework.http.HttpStatus;

public class TempTokenExpiredException extends CommonException {
    public TempTokenExpiredException() {
        super("Expired.tempToken", HttpStatus.UNAUTHORIZED);
        setErrorCode(true);
    }
}
