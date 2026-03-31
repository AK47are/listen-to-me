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
 * 预约订单实体类
 */
@Getter
@Setter
@TableName("consult_order")
@Schema(description = "预约订单")
public class ConsultOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "时间槽ID")
    private Long slotId;

    @Schema(description = "预约用户ID")
    private Long userId;

    @Schema(description = "创作者ID")
    private Long creatorId;

    @Schema(description = "用户留言")
    private String message;

    @Schema(description = "订单状态: PENDING_CONFIRM, CONFIRMED, COMPLETED, CANCELLED, REFUND_PENDING, REFUNDED")
    private String status;

    @Schema(description = "预约地址（确认时填充）")
    private String address;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
