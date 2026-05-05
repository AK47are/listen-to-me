package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PayChannel implements DescribedEnum<String> {
    ALIPAY("alipay", "支付宝"),
    WECHAT("wechat", "微信");

    public static final String SCHEMA_DESC = "alipay, wechat";

    @EnumValue
    private final String code;
    private final String label;

    PayChannel(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
