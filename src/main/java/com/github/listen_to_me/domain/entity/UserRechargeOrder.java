package com.github.listen_to_me.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.github.listen_to_me.common.enumeration.PayChannel;
import com.github.listen_to_me.common.enumeration.RechargePayStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("user_recharge_order")
@Schema(description = "用户虚拟币充值订单")
public class UserRechargeOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    @Schema(description = "充值订单号（RC开头）")
    private String rechargeSn;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "充值金额（元）")
    private Double rechargeAmount;
    @Schema(description = RechargePayStatus.SCHEMA_DESC)
    private RechargePayStatus payStatus;
    @Schema(description = PayChannel.SCHEMA_DESC)
    private PayChannel payChannel;
    private LocalDateTime payTime;
    private LocalDateTime createTime;

}
