package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum RefundStatus implements DescribedEnum<String> {
    PENDING("PENDING", "待处理"),
    PROCESSED("PROCESSED", "已处理");

    public static final String SCHEMA_DESC = "申请状态: PENDING, PROCESSED";

    @EnumValue
    private final String code;
    private final String label;

    RefundStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
