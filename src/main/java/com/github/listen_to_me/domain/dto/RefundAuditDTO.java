package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "审核退款请求")
public class RefundAuditDTO {

    @NotNull(message = "退款申请ID不能为空")
    @Schema(description = "退款申请ID", example = "1")
    private Long applyId;

    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果: true-通过, false-拒绝", example = "true")
    private Boolean approved;

    @Schema(description = "拒绝原因（拒绝时必填）", example = "证据不足")
    private String rejectReason;
}
