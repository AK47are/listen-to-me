package com.github.listen_to_me.domain.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 退款申请表实体类
 */
@Getter
@Setter
@TableName("refund_apply")
@Schema(description = "退款申请")
public class RefundApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "退款申请ID")
    private Long id;

    @Schema(description = "预约订单ID")
    private Long orderId;

    @Schema(description = "申请用户ID")
    private Long userId;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "申请状态: PENDING, PROCESSED")
    private String status;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "处理时间")
    private LocalDateTime updateTime;
}
