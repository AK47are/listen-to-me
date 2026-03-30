package com.github.listen_to_me.domain.vo;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "余额信息")
public class BalanceVO {

    @Schema(description = "当前虚拟币余额", example = "89.60")
    private BigDecimal balance;

    @Schema(description = "累计充值", example = "100.00")
    private BigDecimal totalRecharge;

    @Schema(description = "累计消费", example = "55.70")
    private BigDecimal totalSpent;
}
