package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AiTaskStatus implements DescribedEnum<String> {
    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败");

    public static final String SCHEMA_DESC = "任务状态: PENDING(待处理), PROCESSING(处理中), SUCCESS(成功), FAILED(失败)";

    @EnumValue
    private final String code;
    private final String label;

    AiTaskStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
