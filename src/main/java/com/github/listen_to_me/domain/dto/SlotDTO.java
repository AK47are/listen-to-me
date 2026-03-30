package com.github.listen_to_me.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "时间槽入参")
public class SlotDTO {

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", example = "2026-04-01 10:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", example = "2026-04-01 11:00:00")
    private LocalDateTime endTime;

    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于0")
    @Schema(description = "预约价格（虚拟币）", example = "50")
    private BigDecimal price;

    @NotBlank(message = "地址不能为空")
    @Schema(description = "预约地址", example = "腾讯会议链接xxx")
    private String address;
}
