package com.github.listen_to_me.common.exception;

public class RegisterException extends BaseException {
    public RegisterException(String message) {
        super(message);
    }

    public RegisterException(Integer code, String message) {
        super(code, message);
    }
}
