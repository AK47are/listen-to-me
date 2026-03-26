package com.github.listen_to_me.common.exception;

public class AuthException extends BaseException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(Integer code, String message) {
        super(code, message);
    }

}
