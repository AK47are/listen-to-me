package com.github.listen_to_me.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "预约订单分页查询条件")
public class ConsultPageQuery extends PageQuery {

    @Schema(description = "订单状态（可选）", example = "PENDING_CONFIRM")
    private String status;
}
