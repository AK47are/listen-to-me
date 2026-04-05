package com.github.listen_to_me.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RechargeResultDTO {
    @NotBlank(message = "充值金额不能为空")
    @Positive(message = "充值金额必须大于0")
    @Schema(description = "充值金额")
    private Double amount;

    @NotNull(message = "支付方式不能为空")
    @Schema(description = "支付方式")
    private String paymentMethod;
}
