package com.github.listen_to_me.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    @Size(min = 2, max = 20, message = "昵称长度需在2-20字符之间")
    private String nickname;

    private String avatar;

    @Pattern(regexp = "^1[3-9]\\d{9}$|^$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(min = 6, max = 6, message = "验证码必须为6位")
    private String verifyCode;

    private String oldPassword;

    @Size(min = 6, max = 20, message = "新密码长度需在6-20字符之间")
    private String newPassword;
}
