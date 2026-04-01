package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "预约订单出参")
public class ConsultOrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "时间槽ID")
    private Long slotId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "预约价格")
    private BigDecimal price;

    @Schema(description = "订单状态: PENDING_CONFIRM, CONFIRMED, COMPLETED, CANCELLED, REFUND_PENDING, REFUNDED")
    private String status;

    @Schema(description = "预约地址（用户端需要仅确认后返回）")
    private String address;

    @Schema(description = "创作者ID")
    private Long creatorId;

    @Schema(description = "创作者名称")
    private String creatorName;

    @Schema(description = "创作者头像")
    private String creatorAvatar;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "用户留言")
    private String message;

    @Schema(description = "拒绝原因（仅退款拒绝时返回）")
    private String rejectReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
