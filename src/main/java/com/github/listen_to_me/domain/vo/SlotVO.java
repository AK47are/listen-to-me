package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "时间槽出参")
public class SlotVO {

    @Schema(description = "时间槽ID")
    private Long id;

    @Schema(description = "开始时间", example = "2026-04-01 10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2026-04-01 11:00:00")
    private LocalDateTime endTime;

    @Schema(description = "预约价格（虚拟币）", example = "50")
    private BigDecimal price;

    @Schema(description = "预约地址", example = "腾讯会议链接xxx")
    private String address;

    @Schema(description = "状态: AVAILABLE / BOOKED / EXPIRED / CANCELLED", example = "AVAILABLE")
    private String status;
}
