package com.github.listen_to_me.domain.dto;

import lombok.Data;

@Data
public class VerifyCodeDTO {
    // 手机号或邮箱
    private String target;

    // 图形验证码唯一标识
    private String uuid;

    // 用户输入的图形验证码字符
    private String imageCode;
}
