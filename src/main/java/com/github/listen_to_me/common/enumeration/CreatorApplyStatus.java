package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum CreatorApplyStatus implements DescribedEnum<String> {
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝");

    public static final String SCHEMA_DESC = "申请状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝";

    @EnumValue
    private final String code;
    private final String label;

    CreatorApplyStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
