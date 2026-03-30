package com.github.listen_to_me.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 虚拟币流水表
 */
@Getter
@Setter
@TableName("coin_transaction")
@Schema(description = "虚拟币流水")
public class CoinTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "交易类型: INCOME(收入), EXPENSE(支出)")
    private String type;

    @Schema(description = "来源/去向: RECHARGE(充值), AUDIO(音频购买), CONSULT(咨询预约), REFUND(退款)")
    private String bizType;

    @Schema(description = "变动金额（正数）")
    private BigDecimal amount;

    @Schema(description = "变动前余额")
    private BigDecimal balanceBefore;

    @Schema(description = "变动后余额")
    private BigDecimal balanceAfter;

    @Schema(description = "业务ID（订单号/充值单号）")
    private String bizId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
