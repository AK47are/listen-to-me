package com.github.listen_to_me.domain.vo;

import lombok.Data;

@Data
public class AudioPublishVO {
    private Long audioId;
    // 发布状态（PENDING_TRANSCODE / TRANSCODING / ONLINE / FAILED）
    private String status;
}
