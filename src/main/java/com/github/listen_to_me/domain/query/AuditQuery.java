package com.github.listen_to_me.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "审核查询参数")
public class AuditQuery extends PageQuery {
    /**
     * 枚举值: PENDING / ALL / APPROVED / REJECTED， 默认 PENDING
     */
    private String status = "PENDING";
}
