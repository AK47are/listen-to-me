package com.github.listen_to_me.domain.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String confirmPassword;
    private String phone;
    private String email;
    // 校验码
    private String verifyCode;
}
