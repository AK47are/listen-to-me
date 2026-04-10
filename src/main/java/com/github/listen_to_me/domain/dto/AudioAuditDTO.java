package com.github.listen_to_me.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AudioAuditDTO {
    @NotNull(message = "音频ID不能为空")
    private Long audioId;
    /**
     * 审核结果， 枚举值: APPROVED / REJECTED
     */
    @NotBlank(message = "审核结果不能为空")
    private String status;
    private String rejectReason;
}
