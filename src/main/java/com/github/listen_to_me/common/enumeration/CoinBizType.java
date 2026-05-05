package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum CoinBizType implements DescribedEnum<String> {
    RECHARGE("RECHARGE", "充值"),
    AUDIO("AUDIO", "音频购买"),
    CONSULT("CONSULT", "咨询预约"),
    REFUND("REFUND", "退款");

    public static final String SCHEMA_DESC = "来源/去向: RECHARGE(充值), AUDIO(音频购买), CONSULT(咨询预约), REFUND(退款)";

    @EnumValue
    private final String code;
    private final String label;

    CoinBizType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
