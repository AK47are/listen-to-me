package com.github.listen_to_me.domain.vo;

import lombok.Data;

@Data
public class AudioStatusVO {
    private Long audioId;
    private String status;
    // PENDING_TRANSCODE / TRANSCODING / ONLINE / FAILED
    private String failReason; // 仅 FAILED 时有值
}
