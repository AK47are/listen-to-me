package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ConsultSlotStatus implements DescribedEnum<String> {
    AVAILABLE("AVAILABLE", "可用"),
    BOOKED("BOOKED", "已预约"),
    EXPIRED("EXPIRED", "已过期"),
    CANCELLED("CANCELLED", "已取消");

    public static final String SCHEMA_DESC = "状态: AVAILABLE, BOOKED, EXPIRED, CANCELLED";

    @EnumValue
    private final String code;
    private final String label;

    ConsultSlotStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
