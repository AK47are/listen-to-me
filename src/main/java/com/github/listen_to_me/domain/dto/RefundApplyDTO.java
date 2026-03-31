package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 暂时只有一个字段，后续可以支持照片等等
@Data
@Schema(description = "申请退款请求")
public class RefundApplyDTO {

    @NotBlank(message = "退款原因不能为空")
    @Schema(description = "退款原因", example = "咨询质量不好，未解决我的问题")
    private String reason;
}
