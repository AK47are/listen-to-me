package com.github.listen_to_me.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "退款申请分页查询条件")
public class RefundPageQuery extends PageQuery {

    @Schema(description = "申请状态: PENDING, PROCESSED", example = "PENDING")
    private String status;
}
