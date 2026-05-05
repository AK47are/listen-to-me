package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum UserStatus implements DescribedEnum<String> {
    NORMAL("NORMAL", "正常"),
    BANNED("BANNED", "封禁");

    public static final String SCHEMA_DESC = "状态: NORMAL(正常), BANNED(封禁)";

    @EnumValue
    private final String code;
    private final String label;

    UserStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
