
package org.advisor.member.exceptions;

import org.advisor.global.exceptions.CommonException;
import org.springframework.http.HttpStatus;

public class TempTokenNotFoundException extends CommonException {
    public TempTokenNotFoundException() {
        super("NotFound.tempToken", HttpStatus.NOT_FOUND);
        setErrorCode(true);
    }
}
