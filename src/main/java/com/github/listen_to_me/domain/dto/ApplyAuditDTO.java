package com.github.listen_to_me.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyAuditDTO {
    @NotNull(message = "申请ID不能为空")
    private Long applyId;
    @NotBlank(message = "审核状态不能为空")
    private String status;
    private String rejectReason;
}
