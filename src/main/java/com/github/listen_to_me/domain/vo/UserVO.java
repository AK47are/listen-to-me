package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Boolean isCreator;
    private String status;
    private LocalDateTime createTime;
}
