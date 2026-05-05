package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PayStatus implements DescribedEnum<Integer> {
    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    CANCELLED(2, "已取消");

    public static final String SCHEMA_DESC = "0-待支付, 1-已支付, 2-已取消";

    @EnumValue
    private final Integer code;
    private final String label;

    PayStatus(Integer code, String label) {
        this.code = code;
        this.label = label;
    }
}
