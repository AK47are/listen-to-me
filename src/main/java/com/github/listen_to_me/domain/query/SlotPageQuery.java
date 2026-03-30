package com.github.listen_to_me.domain.query;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 时间槽分页查询对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "时间槽分页查询条件")
public class SlotPageQuery extends PageQuery {

    @Schema(description = "开始时间（可选）", example = "2026-04-01 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间（可选）", example = "2026-04-30 23:59:59")
    private LocalDateTime endTime;
}
