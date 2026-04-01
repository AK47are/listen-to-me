package com.github.listen_to_me.domain.query;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "预约订单分页查询条件")
public class ConsultPageQuery extends PageQuery {

    @Schema(description = "订单状态（可选）", example = "PENDING_CONFIRM")
    private String status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间（可选）", example = "2026-04-01 00:00:00")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间（可选）", example = "2026-04-30 23:59:59")
    private LocalDateTime endTime;
}
