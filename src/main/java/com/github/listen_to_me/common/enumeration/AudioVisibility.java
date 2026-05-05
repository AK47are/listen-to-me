package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AudioVisibility implements DescribedEnum<String> {
    PUBLIC("PUBLIC", "公开可见"),
    PRIVATE("PRIVATE", "仅自己/管理员可见");

    public static final String SCHEMA_DESC = "可见性：PUBLIC-公开可见 PRIVATE-仅自己/管理员可见";

    @EnumValue
    private final String code;
    private final String label;

    AudioVisibility(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
