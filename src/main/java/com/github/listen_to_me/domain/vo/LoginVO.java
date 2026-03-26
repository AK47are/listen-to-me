package com.github.listen_to_me.domain.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private UserVO userInfo;
}
