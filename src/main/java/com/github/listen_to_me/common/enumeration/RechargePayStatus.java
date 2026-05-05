package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum RechargePayStatus implements DescribedEnum<String> {
    PENDING("PENDING", "待支付"),
    SUCCESS("SUCCESS", "已支付");

    public static final String SCHEMA_DESC = "支付状态：PENDING-待支付，SUCCESS-已支付";

    @EnumValue
    private final String code;
    private final String label;

    RechargePayStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
