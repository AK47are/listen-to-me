package com.github.listen_to_me.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RechargeOrderVO {
    private String orderSn;
    private Double amount;
    private String status;
    private String payChannel;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
}
