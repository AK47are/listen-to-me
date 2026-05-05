package com.github.listen_to_me.common.enumeration;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AudioPublishStatus implements DescribedEnum<String> {
    PENDING_TRANSCODE("PENDING_TRANSCODE", "待转码"),
    TRANSCODING("TRANSCODING", "转码中"),
    ONLINE("ONLINE", "已上线"),
    FAILED("FAILED", "转码失败");

    public static final String SCHEMA_DESC = "发布状态: PENDING_TRANSCODE(待转码), TRANSCODING(转码中), ONLINE(已上线), FAILED(转码失败)";

    @EnumValue
    private final String code;
    private final String label;

    AudioPublishStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
