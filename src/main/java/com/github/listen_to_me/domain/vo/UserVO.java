package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private BigDecimal balance;
    private BigDecimal frozenBalance;

    /**
     * 创作者标识：false-听众, true-创作者
     * 前端根据此字段决定是否展示「创作中心」入口
     */
    private Boolean isCreator;
}
