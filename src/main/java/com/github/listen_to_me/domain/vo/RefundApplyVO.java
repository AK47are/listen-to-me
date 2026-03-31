package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "退款申请出参")
public class RefundApplyVO {

    @Schema(description = "退款申请ID")
    private Long id;

    @Schema(description = "预约订单ID")
    private Long orderId;

    @Schema(description = "时间槽ID")
    private Long slotId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "预约价格")
    private BigDecimal price;

    @Schema(description = "订单状态（申请时的状态）")
    private String orderStatus;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "创作者ID")
    private Long creatorId;

    @Schema(description = "创作者名称")
    private String creatorName;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "申请时间")
    private LocalDateTime applyTime;
}
