package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CoinTransactionVO {

    private Long id;
    /**
     * 交易类型
     * 枚举值:EXPENSE／INCOME
     */
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String bizId;
    /**
     * 业务类型，
     * 枚举值:AUDIO／CONSULT／RECHARGE／REFUND
     */
    private String bizType;
    private String remark;
    private LocalDateTime createTime;
}
