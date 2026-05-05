package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AiTaskType implements DescribedEnum<String> {
    TRANSCRIPTION("TRANSCRIPTION", "转写"),
    SUMMARIZATION("SUMMARIZATION", "摘要"),
    SLOT_GENERATION("SLOT_GENERATION", "时间槽生成");

    public static final String SCHEMA_DESC = "任务类型: TRANSCRIPTION(转写), SUMMARIZATION(摘要), SLOT_GENERATION(时间槽生成)";

    @EnumValue
    private final String code;
    private final String label;

    AiTaskType(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
