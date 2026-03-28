package com.github.listen_to_me.domain.dto;

import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private String verifyCode;
    private String oldPassword;
    private String newPassword;
}
