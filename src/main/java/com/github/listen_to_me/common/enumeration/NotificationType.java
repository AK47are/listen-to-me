package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum NotificationType implements DescribedEnum<String> {
    AUDIT_PASS("AUDIT_PASS", "审核通过"),
    AUDIT_REJECT("AUDIT_REJECT", "审核未通过"),
    CREATOR_VERIFY_PASS("CREATOR_VERIFY_PASS", "创作者审核通过"),
    CREATOR_VERIFY_REJECT("CREATOR_VERIFY_REJECT", "创作者审核未通过"),
    SYSTEM("SYSTEM", "系统通知");

    public static final String SCHEMA_DESC = "通知类型: AUDIT_PASS/AUDIT_REJECT/CREATOR_VERIFY_PASS/CREATOR_VERIFY_REJECT/SYSTEM";

    @EnumValue
    private final String code;
    private final String label;

    NotificationType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
