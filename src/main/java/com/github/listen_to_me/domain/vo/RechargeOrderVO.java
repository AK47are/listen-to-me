package com.github.listen_to_me.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RechargeOrderVO {
    private String orderSn;
    private Double amount;
    private String status;
    private String payChannel;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
